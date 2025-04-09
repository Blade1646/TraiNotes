using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace TraiNotes_API.Models
{
    public class Approach
    {
        //Main attributes
        [Key]
        public int Id { get; set; }
        public float Weight { get; set; }
        public int Reps { get; set; }

        //Relationships
        public int ExerciseId { get; set; }
        [ForeignKey("ExerciseId")]
        public Exercise Exercise { get; set; }
        public int TrainingDayId { get; set; }
        [ForeignKey("TrainingDayId")]
        public TrainingDay TrainingDay { get; set; }

    }
}
