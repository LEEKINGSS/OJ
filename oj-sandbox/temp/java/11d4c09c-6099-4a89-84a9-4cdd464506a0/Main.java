import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static void get11(StringBuilder doc){
        int w=0,l=0;
        for(int i=0;i<doc.length();i++){
            if(doc.charAt(i)=='W')w++;
            else if(doc.charAt(i)=='L')l++;
            else{
                System.out.println(w+":"+l);
                break;
            }
            if((w>=11||l>=11)&&(w-l>=2||l-w>=2)){
                System.out.println(w+":"+l);
                w=0;
                l=0;
            }
        }
    }
    public static void get21(StringBuilder doc){
        int w=0,l=0;
        for(int i=0;i<doc.length();i++){
            if(doc.charAt(i)=='W')w++;
            else if(doc.charAt(i)=='L')l++;
            else{
                System.out.println(w+":"+l);
                break;
            }
            if((w>=21||l>=21)&&(w-l>=2||l-w>=2)){
                System.out.println(w+":"+l);
                w=0;
                l=0;
            }
        }
    }
    public static void main(String[]args){
        StringBuilder doc=new StringBuilder("");
        while(scanner.hasNextLine()){
            StringBuilder temp= new StringBuilder(scanner.nextLine());
            if(temp.isEmpty())break;
            doc=doc.append(temp);
        }
        get11(doc);
        System.out.println();
        get21(doc);
    }

