using System; 

namespace MetalConstruction.instances { 
    public class Objects{ 
        private List<Sketch> sketchesList = new List<Sketch>(); 
        private double paintedSurfaceIn_mp = 0;

        public void AddSketch(Sketch sketch) { 
            sketchesList.Add(sketch);  
            paintedSurfaceIn_mp += sketch.GetSurfaceForThisInstance();
        }

        public void RemoveSketch(Sketch sketch) { 
            sketchesList.Remove(sketch);  
            paintedSurfaceIn_mp -= sketch.GetSurfaceForThisInstance();
        }

        public double GetSurfaceForThisInstance() { 
            return paintedSurfaceIn_mp;
        }

        public void ExportDataToCSV(string filePath, string currentId, string parentId, StreamWriter writer) { 
            writer.WriteLine($"{currentId},obiect,{parentId},{GetSurfaceForThisInstance()} mp");

            int index = 1;
            foreach (Sketch sketch in sketchesList) { 
                string sketchId = "plansa_" + index;
                sketch.ExportDataToCSV(filePath, sketchId, currentId, writer); 
                
                index++;
            }
        }
    }
}