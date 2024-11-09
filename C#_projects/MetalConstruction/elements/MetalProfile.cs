using System; 

namespace MetalConstruction.elements { 
    public class MetalProfile : ActualElement{ 
        private string nameOfProfile;
        private double lengthIn_cm;  
        private double widthIn_cm; 

        private static double ExtractDouble(string input) {
            string numberAsString = string.Empty;
            foreach (char c in input) {
                if (char.IsDigit(c) || c == '.') {
                    numberAsString += c;
                }
            }

            double result;
            if (!double.TryParse(numberAsString, out result)) {
                throw new ArgumentException($"Invalid numeric value from input '{input}' !\n");
            }
            return result;
        }

        private void ParseNameOfProfile(string nameOfProfile) { 
            string[] parts = nameOfProfile.Split('x'); 
            if (parts.Length != 2) { 
                throw new ArgumentException("Invalid name of profile. Character 'x' must split length and width !\n"); 
            }
            
            lengthIn_cm = ExtractDouble(parts[0]); 
            widthIn_cm = ExtractDouble(parts[1]);
        }

        public MetalProfile(string nameOfProfile) {  
            this.nameOfProfile = nameOfProfile;
            ParseNameOfProfile(nameOfProfile); 
        }

        public override double GetPaintedSurfaceIn_mp() { 
            return lengthIn_cm * widthIn_cm / 10000 * 6; //use same unit for all dimensions
        }

        public override void ExportDataToCSV(string filePath, string parentId, StreamWriter writer) { 
            writer.WriteLine($"{nameOfProfile},profil_metalic, {parentId}, {GetPaintedSurfaceIn_mp()} mp"); //use '-' for id because is the leaf of our tree representation
        }
    } 
}