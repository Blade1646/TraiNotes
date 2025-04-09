using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace TraiNotes_API.Models
{
    public class Exercise
    {
        //Main attributes
        [Key]
        public int Id { get; set; }
        public string Name { get; set; }
        public string? Notes { get; set; }
        public bool Favourite { get; set; }

        //Relationships
        public int CategoryId { get; set; }
        [ForeignKey("CategoryId")]
        public Category Category { get; set; }
        public ICollection<Approach>? Approaches { get; set; }
    }
}