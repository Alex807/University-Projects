using MetalConstruction.constants;
using MetalConstruction.input; 
using MetalConstruction.instances; 
using MetalConstruction.elements;

namespace MetalConstruction.envirement {
    public static class SetUpAPIEnvirement {
        public static void PrintWelcomeMessage() { 
            Console.WriteLine("\n \t\t\t Welcome to 'Metal_Construction' API !! \n");  
            Console.WriteLine("This is an API for creating and handling a construction with multiple projects types."); 
            Console.WriteLine("  # You can export all data into a custom CSV file and save it in any place #\n\n");
        }

        public static void SetConstants() {
            string outputPath = InputValidation.GetOutputPath(); 
            string outputFileName = InputValidation.GetOutputFileName(); 
            Constants.outputPath = outputPath + "\\" + outputFileName;

            Constants.constructionName = InputValidation.GetBuildingName();
        }

        public static void CreateInstances() { 
            var project = new Project("FUNDATIE"); 
            var project1 = new Project("GRINZI");

            var obj = new Objects();

            var sketch = new Sketch();

            var mainPosition = new MainPosition(); 
            var mainPosition1 = new MainPosition();

            var secondaryPosition = new SecondaryPosition(); 
            var secondaryPosition2 = new SecondaryPosition();

            var profile = new MetalProfile("HEA10x1000"); // 6mp
            var flange = new MetalFlange(500000, 2, 10, 0, 2); // 2mp
            var screw = new Screw(1); //1mp

            secondaryPosition.AddElement(profile);
            secondaryPosition.AddElement(flange);
            secondaryPosition.AddElement(screw); 

            secondaryPosition2.AddElement(profile);
            secondaryPosition2.AddElement(flange);
            secondaryPosition2.AddElement(screw);

            mainPosition.AddPosition(secondaryPosition); //18mp
            mainPosition1.AddPosition(secondaryPosition2); //18mp
            
            sketch.AddPosition(mainPosition); 
            sketch.AddPosition(mainPosition1);//36mp

            obj.AddSketch(sketch);

            project.AddObject(obj);  
            project.AddObject(obj);  
            project.AddObject(obj);  
            project.AddObject(obj);  
            project.AddObject(obj);  

            project1.AddObject(obj);

            Construction.AddProject(project1); 
            Construction.AddProject(project); 

            Construction.ExportDataToCSV(); 
        }
    }
}