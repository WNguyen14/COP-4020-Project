package edu.ufl.cise.plc;

class CodeGenStringBuilder {
    StringBuilder delegate = new StringBuilder();
    //methods reimplemented—just call the delegates method
       public CodeGenStringBuilder append(String s){
           delegate.append(s);
           return this;
       }
       //new methods
       public CodeGenStringBuilder comma(){
            delegate.append(",");
            return this;
       }
       public CodeGenStringBuilder semi(){
           delegate.append(";");
           return this;
      }
       public CodeGenStringBuilder newline(){
           delegate.append("\n");
           return this;
      }
}