using System; 

namespace MetalConstruction.elements { 
    public abstract class ActualElement { 
        public abstract double GetPaintedSurfaceIn_mp(); 
        public abstract void ExportDataToCSV(string filePath, string parentId, StreamWriter writer);
    } 
}