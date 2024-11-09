using System; 

namespace MetalConstruction.elements { 
    public class Screw (double surface) : ActualElement{ 
        private double paintedSurfaceIn_mp = surface; 

        public override double GetPaintedSurfaceIn_mp() { 
            return paintedSurfaceIn_mp; 
        }

        public override void ExportDataToCSV(string filePath, string parentId, StreamWriter writer) { 
            writer.WriteLine($"surub, surub_metalic,{parentId}, {paintedSurfaceIn_mp} mp"); //use '-' for id because is the leaf of our tree representation
        }
    } 
}