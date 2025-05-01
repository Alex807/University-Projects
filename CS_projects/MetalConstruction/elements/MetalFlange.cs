using System; 

namespace MetalConstruction.elements { 
    public class MetalFlange : ActualElement{ 
        private double lengthIn_mm;  
        private double widthIn_mm; 
        private double thicknessIn_mm; 
        private double croppedSurfaceIn_mp; 
        private int numberOfPaintedSides; //this can be only 1 or 2 

        public MetalFlange(double lengthIn_mm, double widthIn_mm, double thicknessIn_mm, double croppedSurfaceIn_mp, int numberOfPaintedSides) { 
            if (numberOfPaintedSides != 1 && numberOfPaintedSides != 2) { 
                throw new ArgumentException("Number of painted sides must be 1 or 2\n"); //throw STOP process of creating an invalid object
            }
            this.lengthIn_mm = lengthIn_mm; 
            this.widthIn_mm = widthIn_mm; 
            this.thicknessIn_mm = thicknessIn_mm; 
            this.croppedSurfaceIn_mp = croppedSurfaceIn_mp; 
            this.numberOfPaintedSides = numberOfPaintedSides;
        }

        public override double GetPaintedSurfaceIn_mp() { 
            return  lengthIn_mm * widthIn_mm / 1000000 * numberOfPaintedSides - croppedSurfaceIn_mp; //use same unit for all dimensions
        }

        public override void ExportDataToCSV(string filePath, string parentId, StreamWriter writer) { 
            writer.WriteLine($"flansa{lengthIn_mm}x{widthIn_mm},flansa_metalica,{parentId}, {GetPaintedSurfaceIn_mp()} mp"); //use '-' for id because is the leaf of our tree representation
        }

    } 
}