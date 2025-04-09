using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using Microsoft.EntityFrameworkCore;
using TraiNotes_API.Data;
using TraiNotes_API.Models;

namespace TraiNotes_API.Controllers
{
    [Route("api/")]
    [ApiController]
    public class ApiController : ControllerBase
    {
        private readonly ApiDbContext _context;

        public ApiController(ApiDbContext context) => _context = context;

        [HttpGet("users/login")]
        public async Task<IActionResult> LoginUser(string email, string password)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);

            if (user == null)
            {
                return NotFound();
            }

            if (user.Password != password)
            {
                return Unauthorized();
            }

            return Ok(new { UserId = user.Id });
        }

        [HttpGet("users/get")]
        public async Task<IActionResult> GetUserById(int id)
        {
            var user = await _context.Users.FindAsync(id);

            return user == null ? NotFound() : Ok(user);
        }

        [HttpGet("categories/get")]
        public async Task<IActionResult> GetCategoryById(int id)
        {
            var category = await _context.Categories.FindAsync(id);

            return category == null ? NotFound() : Ok(category);
        }

        [HttpGet("categories/getByUserId")]
        public async Task<IActionResult> GetCategoriesByUserId(int userId)
        {
            var user = await _context.Users.Include(u => u.Categories).FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound();
            }

            var categories = user.Categories.Select(c => new { c.Id, c.Name }).ToList();
            return Ok(categories);
        }

        [HttpGet("exercises/get")]
        public async Task<IActionResult> GetExerciseById(int id)
        {
            var exercise = await _context.Exercises.FindAsync(id);

            return exercise == null ? NotFound() : Ok(exercise);
        }

        [HttpGet("exercises/getByCategoryId")]
        public async Task<IActionResult> GetExercisesByCategoryId(int categoryId)
        {
            var category = await _context.Categories.Include(c => c.Exercises).FirstOrDefaultAsync(c => c.Id == categoryId);

            if (category == null)
            {
                return NotFound();
            }

            var exercises = category.Exercises.Select(e => new { e.Id, e.Name, e.Notes, e.Favourite, e.CategoryId }).ToList();
            return Ok(exercises);
        }

        [HttpGet("exercises/getByTrainingDay")]
        public async Task<IActionResult> GetExerciseByTrainingDay(int id, DateTime trainingDay)
        {
            var exercise = await _context.Exercises
            .Include(e => e.Approaches.Where(a => a.TrainingDay.Day == trainingDay))
            .Where(e => e.Id == id)
            .FirstOrDefaultAsync();

            if (exercise == null)
            {
                return NotFound();
            }

            return Ok(exercise);
        }

        [HttpGet("approaches/get")]
        public async Task<IActionResult> GetApproachById(int id)
        {
            var approach = await _context.Approaches.FindAsync(id);

            return approach == null ? NotFound() : Ok(approach);
        }
        
        [HttpGet("approaches/getByTrainingDay")]
        public async Task<IActionResult> GetApproachesByTrainingDay(DateTime trainingDay, int userId)
        {
            var user = await _context.Users
            .Include(u => u.Categories)
            .ThenInclude(c => c.Exercises)
            .ThenInclude(e => e.Approaches.Where(a => a.TrainingDay.Day == trainingDay))
            .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound();
            }

            var exercises = user.Categories
            .SelectMany(c => c.Exercises
                .Where(e => e.Approaches.Any())
                .Select(e => new
                {
                    e.Id,
                    e.Name,
                    Approaches = e.Approaches.Select(a => new
                    {
                        a.Weight,
                        a.Reps
                    })
                })
            )
            .ToList();

            return Ok(exercises);
        }
        
        [HttpGet("trainingDays/get")]
        public async Task<IActionResult> GetTrainingDayById(int id)
        {
            var trainingDay = await _context.TrainingDays.FindAsync(id);

            return trainingDay == null ? NotFound() : Ok(trainingDay);
        }

        [HttpPost("categories/add")]
        public async Task<IActionResult> AddCategory(string name, int userId)
        {
            if (await _context.Categories
                .AnyAsync(c => c.Name == name && c.UserId == userId)) return Conflict();

            Category category = new Category { Name = name, UserId = userId };
            await _context.Categories.AddAsync(category);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetCategoryById), new { id = category.Id }, category);
        }

        [HttpPost("exercises/add")]
        public async Task<IActionResult> AddExercise(string name, string? notes, int userId, int categoryId)
        {
            if (await _context.Exercises
                .AnyAsync(e => e.Name == name && e.Category.UserId == userId)) return Conflict();

            Exercise exercise = new Exercise { Name = name, Notes = notes, Favourite = false, CategoryId = categoryId};
            await _context.Exercises.AddAsync(exercise);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetExerciseById), new { id = exercise.Id }, exercise);
        }
        
        [HttpPost("approaches/add")]
        public async Task<IActionResult> AddApproach(float weight, int reps, DateTime trainingDayDay, int exerciseId)
        {
            var trainingDay = await _context.TrainingDays
            .Where(t => t.Day == trainingDayDay)
            .FirstOrDefaultAsync();

            if (trainingDay == null)
            {
                trainingDay = new TrainingDay { Day = trainingDayDay };
                await _context.TrainingDays.AddAsync(trainingDay);
                await _context.SaveChangesAsync();
            }
            
            Approach approach = new Approach { Weight = weight, Reps = reps,
                ExerciseId = exerciseId, TrainingDayId = trainingDay.Id };

            await _context.Approaches.AddAsync(approach);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetApproachById), new { id = approach.Id }, approach);
        }
        
        [HttpPut("users/update/password")]
        public async Task<IActionResult> UpdateUserPassword(int id, string password)
        {
            var user = await _context.Users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            user.Password = password;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpPut("users/update/profile")]
        public async Task<IActionResult> UpdateUserProfile(int id, string nickname, string imageURL)
        {
            var user = await _context.Users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            if (await _context.Users.AnyAsync(u => u.Id != id && u.Nickname == nickname))
            {
                return Conflict();
            }

            user.Nickname = nickname;
            user.ImageURL = imageURL;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpPut("categories/edit")]
        public async Task<IActionResult> EditCategory(int id, string name, int userId)
        {
            var category = await _context.Categories.FindAsync(id);

            if (category == null)
            {
                return NotFound();
            }

            if (await _context.Categories.AnyAsync(c => c.Id != id && c.Name == name && c.UserId == userId))
            {
                return Conflict();
            }

            category.Name = name;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpPut("exercises/edit")]
        public async Task<IActionResult> EditExercise(int id, string name, string? notes, int userId)
        {
            var exercise = await _context.Exercises.FindAsync(id);

            if (exercise == null)
            {
                return NotFound();
            }

            if (await _context.Exercises.AnyAsync(e => e.Id != id && e.Name == name && e.Category.UserId == userId))
            {
                return Conflict();
            }

            exercise.Name = name;
            exercise.Notes = notes;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpPut("exercises/edit/favourite")]
        public async Task<IActionResult> EditExerciseFavourite(int id, bool value)
        {
            var exercise = await _context.Exercises.FindAsync(id);

            if (exercise == null)
            {
                return NotFound();
            }

            exercise.Favourite = value;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpPut("approaches/edit")]
        public async Task<IActionResult> EditApproach(int id, float weight, int reps)
        {
            var approach = await _context.Approaches.FindAsync(id);

            if (approach == null)
            {
                return NotFound();
            }

            approach.Weight = weight;
            approach.Reps = reps;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpDelete("categories/delete")]
        public async Task<IActionResult> DeleteCategory(int id)
        {
            var categoryToDelete = await _context.Categories.FindAsync(id);
            if (categoryToDelete == null) return NotFound();
            _context.Categories.Remove(categoryToDelete);

            await _context.SaveChangesAsync();
            return NoContent();
        }

        [HttpDelete("exercises/delete")]
        public async Task<IActionResult> DeleteExercise(int id)
        {
            var exerciseToDelete = await _context.Exercises.FindAsync(id);
            if (exerciseToDelete == null) return NotFound();
            _context.Exercises.Remove(exerciseToDelete);

            await _context.SaveChangesAsync();
            return NoContent();
        }

        [HttpDelete("approaches/delete")]
        public async Task<IActionResult> DeleteApproach(int id)
        {
            var approachToDelete = await _context.Approaches.FindAsync(id);
            if (approachToDelete == null) return NotFound();
            _context.Approaches.Remove(approachToDelete);

            await _context.SaveChangesAsync();
            return NoContent();
        }
    }
}