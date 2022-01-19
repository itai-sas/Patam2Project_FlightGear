 package utils;;

public class Calculate
{
    public static String getTimeString(int timeInSeconds)
    {
        String hours="",minutes="",seconds="";
        int h = timeInSeconds/3600;
        int m= timeInSeconds % 3600 / 60;
        int s = timeInSeconds %60;

        if(h<10)
        {
            hours = "0" + h;
        }
        else
        {
            hours+=h;
        }
        if(m<10)
        {
            minutes ="0" + m;
        }
        else
        {
            minutes+=m;
        }
        if(s<10)
        {
            seconds="0" +s;
        }
        else
        {
            seconds+=s;
        }
        return hours+":"+minutes+":"+seconds;
    }
}
