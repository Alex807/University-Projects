using System;  

namespace MetalConstruction.instances { 
    public class MainPosition { 
        private List<SecondaryPosition> secondaryPositionsList = new List<SecondaryPosition>();   
        private double paintedSurfaceIn_mp = 0; 

        public void AddPosition(SecondaryPosition position) { 
            secondaryPositionsList.Add(position);  
            paintedSurfaceIn_mp += position.GetSurfaceForThisInstance();
        }

        public void RemovePosition(SecondaryPosition position) { 
            secondaryPositionsList.Remove(position);  
            paintedSurfaceIn_mp -= position.GetSurfaceForThisInstance();
        }

        public double GetSurfaceForThisInstance() { 
            return paintedSurfaceIn_mp; 
        }

        public void ExportDataToCSV(string filePath, string currentId, string parentId, StreamWriter writer) { 
            writer.WriteLine($"{currentId},poz_principala,{parentId},{GetSurfaceForThisInstance()} mp");

            int index = 1;
            foreach (SecondaryPosition secondary in secondaryPositionsList) { 
                string secondaryId = "poz_secundara_" + index;
                secondary.ExportDataToCSV(filePath, secondaryId, currentId, writer); 
                
                index++;
            }
        }
    }
}