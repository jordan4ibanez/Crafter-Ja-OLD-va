package engine.hud;

public class TextHandling {

    private static final float FONT_WIDTH = 216f;
    private static final float LETTER_WIDTH = 6f;

    private static final float FONT_HEIGHT = 16f;
    private static final float LETTER_HEIGHT = 8f;

    public static float[] translateCharToArray(char thisChar){
        float[] letterArray = new float[]{0,0};
        switch (thisChar){
            case 'a':
                letterArray[1] = 1;
                break;
            case 'A':
                break;

            case 'b':
                letterArray[0] = 1;
                letterArray[1] = 1;
                break;
            case 'B':
                letterArray[0] = 1;
                letterArray[1] = 0;
                break;

            case 'c':
                letterArray[0] = 2;
                letterArray[1] = 1;
                break;
            case 'C':
                letterArray[0] = 2;
                letterArray[1] = 0;
                break;

            case 'd':
                letterArray[0] = 3;
                letterArray[1] = 1;
                break;
            case 'D':
                letterArray[0] = 3;
                letterArray[1] = 0;
                break;

            case 'e':
                letterArray[0] = 4;
                letterArray[1] = 1;
                break;
            case 'E':
                letterArray[0] = 4;
                letterArray[1] = 0;
                break;

            case 'f':
                letterArray[0] = 5;
                letterArray[1] = 1;
                break;
            case 'F':
                letterArray[0] = 5;
                letterArray[1] = 0;
                break;

            case 'g':
                letterArray[0] = 6;
                letterArray[1] = 1;
                break;
            case 'G':
                letterArray[0] = 6;
                letterArray[1] = 0;
                break;

            case 'h':
                letterArray[0] = 7;
                letterArray[1] = 1;
                break;
            case 'H':
                letterArray[0] = 7;
                letterArray[1] = 0;
                break;

            case 'i':
                letterArray[0] = 8;
                letterArray[1] = 1;
                break;
            case 'I':
                letterArray[0] = 8;
                letterArray[1] = 0;
                break;

            case 'j':
                letterArray[0] = 9;
                letterArray[1] = 1;
                break;
            case 'J':
                letterArray[0] = 9;
                letterArray[1] = 0;
                break;

            case 'k':
                letterArray[0] = 10;
                letterArray[1] = 1;
                break;
            case 'K':
                letterArray[0] = 10;
                letterArray[1] = 0;
                break;

            case 'l':
                letterArray[0] = 11;
                letterArray[1] = 1;
                break;
            case 'L':
                letterArray[0] = 11;
                letterArray[1] = 0;
                break;

            case 'm':
                letterArray[0] = 12;
                letterArray[1] = 1;
                break;
            case 'M':
                letterArray[0] = 12;
                letterArray[1] = 0;
                break;

            case 'n':
                letterArray[0] = 13;
                letterArray[1] = 1;
                break;
            case 'N':
                letterArray[0] = 13;
                letterArray[1] = 0;
                break;

            case 'o':
                letterArray[0] = 14;
                letterArray[1] = 1;
                break;
            case 'O':
                letterArray[0] = 14;
                letterArray[1] = 0;
                break;

            case 'p':
                letterArray[0] = 15;
                letterArray[1] = 1;
                break;
            case 'P':
                letterArray[0] = 15;
                letterArray[1] = 0;
                break;

            case 'q':
                letterArray[0] = 16;
                letterArray[1] = 1;
                break;
            case 'Q':
                letterArray[0] = 16;
                letterArray[1] = 0;
                break;

            case 'r':
                letterArray[0] = 17;
                letterArray[1] = 1;
                break;
            case 'R':
                letterArray[0] = 17;
                letterArray[1] = 0;
                break;

            case 's':
                letterArray[0] = 18;
                letterArray[1] = 1;
                break;
            case 'S':
                letterArray[0] = 18;
                letterArray[1] = 0;
                break;

            case 't':
                letterArray[0] = 19;
                letterArray[1] = 1;
                break;
            case 'T':
                letterArray[0] = 19;
                letterArray[1] = 0;
                break;

            case 'u':
                letterArray[0] = 20;
                letterArray[1] = 1;
                break;
            case 'U':
                letterArray[0] = 20;
                letterArray[1] = 0;
                break;

            case 'v':
                letterArray[0] = 21;
                letterArray[1] = 1;
                break;
            case 'V':
                letterArray[0] = 21;
                letterArray[1] = 0;
                break;

            case 'w':
                letterArray[0] = 22;
                letterArray[1] = 1;
                break;
            case 'W':
                letterArray[0] = 22;
                letterArray[1] = 0;
                break;

            case 'x':
                letterArray[0] = 23;
                letterArray[1] = 1;
                break;
            case 'X':
                letterArray[0] = 23;
                letterArray[1] = 0;
                break;

            case 'y':
                letterArray[0] = 24;
                letterArray[1] = 1;
                break;
            case 'Y':
                letterArray[0] = 24;
                letterArray[1] = 0;
                break;

            case 'z':
                letterArray[0] = 25;
                letterArray[1] = 1;
                break;
            case 'Z':
                letterArray[0] = 25;
                letterArray[1] = 0;
                break;
            //now I know my ABCs

            case '0':
                letterArray[0] = 26;
                break;
            case '1':
                letterArray[0] = 27;
                break;
            case '2':
                letterArray[0] = 28;
                break;
            case '3':
                letterArray[0] = 29;
                break;
            case '4':
                letterArray[0] = 30;
                break;
            case '5':
                letterArray[0] = 31;
                break;
            case '6':
                letterArray[0] = 32;
                break;
            case '7':
                letterArray[0] = 33;
                break;
            case '8':
                letterArray[0] = 34;
                break;
            case '9':
                letterArray[0] = 35;
                break;

            case '.':
                letterArray[0] = 26;
                letterArray[1] = 1;
                break;
            case '!':
                letterArray[0] = 27;
                letterArray[1] = 1;
                break;
            case '?':
                letterArray[0] = 28;
                letterArray[1] = 1;
                break;

            case ' ':
                letterArray[0] = 29;
                letterArray[1] = 1;
                break;
            case '-':
                letterArray[0] = 30;
                letterArray[1] = 1;
                break;
            case ':':
                letterArray[0] = 31;
                letterArray[1] = 1;
                break;
            default: //all unknown end up as "AAAAAAAAAA"  ¯\_(ツ)_/¯
                break;
        }

        float[] returningArray = new float[4];

        returningArray[0] = (letterArray[0] * LETTER_WIDTH) / FONT_WIDTH; //-x
        returningArray[1] = ((letterArray[0] * LETTER_WIDTH) + LETTER_WIDTH - 1) / FONT_WIDTH; //+x
        returningArray[2] = (letterArray[1] * LETTER_HEIGHT) / FONT_HEIGHT; //-y
        returningArray[3] = ((letterArray[1] * LETTER_HEIGHT) + LETTER_HEIGHT - 1) / FONT_HEIGHT; //+y

        return returningArray;
    }
}
