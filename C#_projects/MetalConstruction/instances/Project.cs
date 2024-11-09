using System;
using System.ComponentModel;
using System.Net.NetworkInformation;

namespace MetalConstruction.instances { 
    public class Project (string nameOfProject) { 
        private string nameOfProject = nameOfProject; 
        private List<Objects> objectsList = new List<Objects>(); 
        private double paintedSurfaceIn_mp = 0;

        public void AddObject(Objects obj) { 
            objectsList.Add(obj);  
            paintedSurfaceIn_mp += obj.GetSurfaceForThisInstance();
        } 

        public void RemoveObject(Objects obj) { 
            objectsList.Remove(obj);  
            paintedSurfaceIn_mp -= obj.GetSurfaceForThisInstance();
        }

        public double GetSurfaceForThisInstance() { 
            return paintedSurfaceIn_mp;
        }

        public void ExportDataToCSV(string filePath, string parentId, StreamWriter writer) {
            writer.WriteLine(", , ,"); //for a better separation of data in CSV file
            writer.WriteLine($"{nameOfProject},proiect,{parentId},{GetSurfaceForThisInstance()} mp");  

            int index = 1;
            foreach (Objects obj in objectsList) { 
                string currentId = "obiectul_" + index;
                obj.ExportDataToCSV(filePath, currentId, nameOfProject, writer); 
                
                index++;
            }
        }
    }
}