using MetalConstruction.instances;
using MetalConstruction.constants;

namespace MetalConstruction { 
    public static class Construction {  
        private static List<Project> projectsList = new List<Project>();  

        public static void AddProject(Project project) { 
            projectsList.Add(project);  
        } 

        public static void RemoveProject(Project project) { 
            projectsList.Remove(project);  
        }

        public static double GetSurfaceForEntireConstruction() { 
            double paintedSurfaceIn_mp = 0;
            foreach (Project project in projectsList) { 
                paintedSurfaceIn_mp += project.GetSurfaceForThisInstance();
            }
            return paintedSurfaceIn_mp;
        }

        public static void ExportDataToCSV() { 
            string constructionName = Constants.constructionName; 

            using (StreamWriter writer = new StreamWriter(Constants.outputPath)) { 
                writer.WriteLine("Id_obiect,Concept,Id_parinte,Aria_vopsita(mp)"); 
                writer.WriteLine($"{constructionName},cladire,-,{GetSurfaceForEntireConstruction()} mp"); 

                foreach (Project project in projectsList) { 
                    project.ExportDataToCSV(Constants.outputPath, constructionName, writer);
                } 
            }
        }
    }
}