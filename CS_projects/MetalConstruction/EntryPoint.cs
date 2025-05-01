using MetalConstruction.elements; 
using MetalConstruction.instances; 
using MetalConstruction.envirement;

namespace MetalConstruction { 
    public class EntryPoint { 
        public static void Main(string[] args) {  
            try { 
                SetUpAPIEnvirement.PrintWelcomeMessage();
                SetUpAPIEnvirement.SetConstants();
                
                SetUpAPIEnvirement.CreateInstances();               

            } catch (System.IO.IOException e) { 
                Console.WriteLine(e.StackTrace);  
                Console.WriteLine(e.Message);   
                Console.WriteLine("Make sure you NOT have the file open in EXCEL.");
            
            } catch (InvalidOperationException e) {
                Console.WriteLine(e.StackTrace); 
                Console.WriteLine(e.Message); 
            } 

        }
    } 
}

