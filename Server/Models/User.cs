using System.ComponentModel.DataAnnotations;

namespace TraiNotes_API.Models
{
    public class User
    {
        //Main attributes
        [Key]
        public int Id { get; set; }
        public string Nickname { get; set; }
        public string? ImageURL { get; set; }
        public int Gender { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        
        //Relationships
        public ICollection<Category>? Categories { get; set; }
    }
}