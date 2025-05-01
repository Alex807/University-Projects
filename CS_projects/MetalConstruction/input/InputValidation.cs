namespace MetalConstruction.input {  
    public static class InputValidation { 
        private static string? ValidateString(string? input) { 
            if (string.IsNullOrEmpty(input)) { 
                throw new ArgumentNullException("Input can NOT be null or empty. Try again !!"); 
            } 
            return input; 
        } 

        private static string? ValidatePath(string? path) { 
            if (!System.IO.Directory.Exists(path)) { 
                throw new System.IO.DirectoryNotFoundException($"Path '{path}' does NOT lead to a directory. Try again !!"); 
            } 
            return path; 
        }

        private static string RemoveInvalidChars(string input) { 
            char[] invalidChars = System.IO.Path.GetInvalidFileNameChars();
            foreach (char c in invalidChars) {   
                if (input.Contains(c)) {
                    throw new ArgumentException($"Invalid character '{c}' in file_name '{input}'. Try again !!"); 
                }
            } 

            if (input.EndsWith(".csv", StringComparison.OrdinalIgnoreCase)) { //check if the input ends with .csv and ignore upper/lower differences
                return input; 
            } else { 
                return input + ".csv"; 
            }
        }

        public static string GetOutputPath() { 
            string? outputPath;
            do { 
                try { 
                    Console.Write("Please enter output_dir path: ");
                    outputPath = ValidateString(Console.ReadLine()); 
                    outputPath = ValidatePath(outputPath);
                 
                } catch (Exception e) { 
                    Console.WriteLine(e.Message); 
                    outputPath = null;   
                }

            } while (outputPath == null);  
            return outputPath;
        }

        public static string GetOutputFileName() { 
            string? outputFileName;
            do { 
                try { 
                    Console.Write("Please enter name of CSV file: ");
                    outputFileName = ValidateString(Console.ReadLine());  
                    if (outputFileName != null) outputFileName = RemoveInvalidChars(outputFileName);
                
                } catch (Exception e) { 
                    Console.WriteLine(e.Message);  
                    outputFileName = null;   
                }

            } while (outputFileName == null);
            return outputFileName;
        }

        public static string GetBuildingName() { 
            string? buildingName;
            do { 
                try { 
                    Console.Write("Please enter name of the building: ");
                    buildingName = ValidateString(Console.ReadLine()); 
                
                } catch (Exception e) { 
                    Console.WriteLine(e.Message); 
                    buildingName = null;   
                }

            } while (buildingName == null); 
            return buildingName;;
        }
    }
}