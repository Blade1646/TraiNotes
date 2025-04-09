    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    namespace TraiNotes_API.Models
    {
        public class Category
        {
            //Main attributes
            [Key]
            public int Id { get; set; }
            public string Name { get; set; }

            //Relationships
            public int UserId { get; set; }
            [ForeignKey("UserId")]
            public User User { get; set; }
            public ICollection<Exercise>? Exercises { get; set; }
        }
    }
