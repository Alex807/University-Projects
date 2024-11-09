using System; 
using MetalConstruction.elements; 

namespace MetalConstruction.instances { 
    public class SecondaryPosition { 
        private List<ActualElement> actualElementsList = new List<ActualElement>();  
        private double paintedSurfaceIn_mp = 0; 

        public void AddElement(ActualElement element) { 
            actualElementsList.Add(element);  
            paintedSurfaceIn_mp += element.GetPaintedSurfaceIn_mp(); //update the surface after each change in list
        }

        public void RemoveElement(ActualElement element) { 
            actualElementsList.Remove(element);  
            paintedSurfaceIn_mp -= element.GetPaintedSurfaceIn_mp();
        }

        public double GetSurfaceForThisInstance() { 
            return paintedSurfaceIn_mp; 
        }

        public void ExportDataToCSV(string filePath, string currentId, string parentId, StreamWriter writer) { 
            writer.WriteLine($"{currentId},poz_secundara,{parentId},{GetSurfaceForThisInstance()} mp");

            foreach (ActualElement element in actualElementsList) { 
                element.ExportDataToCSV(filePath, currentId, writer); 
            }
        }
    } 
}