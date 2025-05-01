using System; 

namespace MetalConstruction.instances { 
    public class Sketch { 
        private List<MainPosition> mainPositionsList = new List<MainPosition>();  
        private double paintedSurfaceIn_mp = 0;

        public void AddPosition(MainPosition position) { 
            mainPositionsList.Add(position);  
            paintedSurfaceIn_mp += position.GetSurfaceForThisInstance();
        }

        public void RemovePosition(MainPosition position) { 
            mainPositionsList.Remove(position);  
            paintedSurfaceIn_mp -= position.GetSurfaceForThisInstance();
        }

        public double GetSurfaceForThisInstance() { 
            return paintedSurfaceIn_mp;
        }

        public void ExportDataToCSV(string filePath, string currentId, string parentId, StreamWriter writer) { 
            writer.WriteLine($"{currentId},plansa,{parentId},{GetSurfaceForThisInstance()} mp");

            int index = 1;
            foreach (MainPosition main in mainPositionsList) { 
                string mainId = "poz_principala_" + index;
                main.ExportDataToCSV(filePath, mainId, currentId, writer); 
                
                index++;
            }
        }
    }
}