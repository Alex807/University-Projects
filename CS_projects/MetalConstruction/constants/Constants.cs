namespace MetalConstruction.constants { 
    public static class Constants {
        private static string _outputPath = "default";
        private static string _constructionName = "default"; 

        public static string outputPath { 
            get { 
                return _outputPath; 
            }  

            set { 
                if (_outputPath.Equals("default")) { 
                    _outputPath = value; 
                
                } else { 
                    throw new InvalidOperationException("Output path is already set, you can do it only once !!");
                }
            }
        }

        public static string constructionName { 
            get { 
                return _constructionName; 
            }  

            set { 
                if (_constructionName.Equals("default")) { 
                    _constructionName = value; 
                
                } else { 
                    throw new InvalidOperationException("Construction name is already set, you can do it only once !!");
                }
            }
        }
    }
}