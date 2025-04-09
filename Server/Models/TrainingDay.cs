using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace TraiNotes_API.Models
{
    public class TrainingDay
    {
        //Main attributes
        [Key]
        public int Id { get; set; }
        [Column(TypeName = "Date")]
        public DateTime Day { get; set; }

        //Relationships
        public ICollection<Approach>? Approaches { get; set; }
    }
}
