/*
Zadanie 2
W niektorych zastosowaniach pozadana jest znajomosc liczby wystapien wzorca p w tekscie s.
 Zakladajac liniowy czas wstepnego przetwarzania tekstu p, opracuj i zaimplementuj algorytm
 ktory bedzie wykomnywal to zadanie w czasie O(|p|).
  */

//Definicje:
//s - tekst, ciąg symboli s = s1,s2,...sn nalezacych do alfabetu
//n - Dlugosc tekstu (liczba jego elementów)
//p - pattern (wzorzec)

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;


public class SuffixTree {

    static String ALPHABET = "";
    static CharSequence s = "";

    public static class Node {

        int begin;
        int end;
        int depth; //distance in characters from root to this node
        Node parent;
        Node suffixLink;

        Map<Character, Node> children;  //zamiast Node[] children

        //Sluzy do wyznaczenia liczby osiagalnych wierzcholkow reprezentujacych sufikssy dla kazdego wezla
        // drzewa sufiksowego
        int numberOfLeaves;             //zliczamy liscie

        Node(int begin, int end, int depth, int noleaf, Node parent) {

            this.begin = begin;
            this.end = end;
            this.depth = depth;
            this.parent = parent;

            children = new HashMap<>();
            numberOfLeaves = noleaf;


        }
    }

    private static Node buildSuffixTree(CharSequence s) {


        //return_s(s.toString());
        SuffixTree.s = s;

        int n = s.length();
        byte[] a = new byte[n];

        for (int i = 0; i < n; i++) {
            a[i] = (byte) ALPHABET.indexOf(s.charAt(i));
        }

        Node root = new Node(0, 0, 0, 0, null);
        Node node = root;

        for (int i = 0, tail = 0; i < n; i++, tail++) {

            //ustaw ostatni stworzony węzeł wewnętrzny na null przed rozpoczęciem każdej fazy.
            Node last = null;

            while (tail >= 0) {
                Node ch = node.children.get(ALPHABET.charAt(a[i - tail]));
                while (ch != null && tail >= ch.end - ch.begin) {

                    //liscie
                    node.numberOfLeaves++;

                    tail -= ch.end - ch.begin;
                    node = ch;
                    ch = ch.children.get(ALPHABET.charAt(a[i - tail]));
                }

                if (ch == null) {
                    // utworz nowy Node z bieżącym znakiem
                    node.children.put(ALPHABET.charAt(a[i]),
                            new Node(i, n, node.depth + node.end - node.begin, 1, node));

                    //liscie
                    node.numberOfLeaves++;

                    if (last != null) {
                        last.suffixLink = node;
                    }
                    last = null;
                } else {
                    byte t = a[ch.begin + tail];
                    if (t == a[i]) {
                        if (last != null) {
                            last.suffixLink = node;
                        }
                        break;
                    } else {
                        Node splitNode = new Node(ch.begin, ch.begin + tail,
                                node.depth + node.end - node.begin, 0, node);
                        splitNode.children.put(ALPHABET.charAt(a[i]),
                                new Node(i, n, ch.depth + tail, 1, splitNode));

                        //liscie
                        splitNode.numberOfLeaves++;

                        splitNode.children.put(ALPHABET.charAt(t), ch);

                        //liscie
                        splitNode.numberOfLeaves += ch.numberOfLeaves;

                        ch.begin += tail;
                        ch.depth += tail;
                        ch.parent = splitNode;
                        node.children.put(ALPHABET.charAt(a[i - tail]), splitNode);

                        //liscie
                        node.numberOfLeaves++;

                        if (last != null) {
                            last.suffixLink = splitNode;
                        }
                        last = splitNode;
                    }
                }
                if (node == root) {
                    --tail;
                } else {
                    node = node.suffixLink;
                }
            }
        }
        return root;
    }


    private static void print(CharSequence s, int i, int j) {
        for (int k = i; k < j; k++) {
            System.out.print(s.charAt(k));
        }
    }

    // Jesli chcemy wydrukowac drzewo nalezy odkomentowac w main
    private static void printTree(Node n, CharSequence s, int spaces) {
        int i;
        for (i = 0; i < spaces; i++) {
            System.out.print("␣");
        }
        print(s, n.begin, n.end);
        System.out.println("␣" + (n.depth + n.end - n.begin));

        for (Node child : n.children.values()) {
            if (child != null) {
                printTree(child, s, spaces + 4);
            }
        }

    }

    /*##########################################################################################*/

    //Szukanie wzorca p (pattern) w tekscie s (text) - Czas O(|p|)

    // Wyszukiwanie wystapien wzorca w drzewie sufiksow rozpoczyna sie od korzenia
    // Nalezy przechodzic drzewo przez krawedzie zgodnie z wyszukiwanym tekstem.
    // Przeszukianie niekoniecznie musi zakonczyc sie w jakims wezle, rownie dobrze
    // moze zakonczyc sie "wewnatrz" krawedzi. Znalezienie wszystkich wystapien wzorca
    // jest rownowazne z liczba wszystkich lisci w podrzewie znajdujacym sie ponizej miejsca
    // w ktorym zakonczono wyszukiwanie wzorca - liscie zliczamy wczesniej w czasie tworzenia
    // drzewa.

    //Zatem:

    // Idac od korzenia wzdluz unikatowej sciezki w T, az
    // (i) dla kolejnej litery dopsaowanie jest niemozliwe - wtedy p nie wystepuje nigdzie w s
    // lub
    // (ii) litery w p wyczerpia sie - Znalezienie wszystkich wystapien wzorca
    // jest rownowazne z liczba wszystkich lisci w podrzewie znajdujacym sie ponizej miejsca
    // w ktorym zakonczono wyszukiwanie wzorca - liscie zliczamy wczesniej w czasie tworzenia
    // drzewa.


    // p - pattern
    //Funkcja wyznacza liczbe wystapien wzorca p w tekscie dla ktorego zostalo skonstruowane drzewo sufiksowe
    private static int patternSearch(Node root, CharSequence p) {
        int index_p = 0;
        int index_s;
        int length_p = p.length();

        Node actualNode = root;


        while (index_p < length_p) {

            actualNode = actualNode.children.get(p.charAt(index_p));

            if (actualNode == null) {
                return 0;
            }

            index_s = actualNode.begin;

            do {
                if (p.charAt(index_p++) != s.charAt(index_s++)) {
                    return 0;
                }
            } while
            (index_s < actualNode.end && index_p <  length_p );
        }
        return actualNode.numberOfLeaves;
    }

    /*##########################################################################################*/

    // funkcja pomocnicza ustawiająca ALPHABET
    private static void saveAlphabet(String s) {
        final Set<Character> set = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            set.add(s.charAt(i));
        }

        StringBuilder alphabetS = new StringBuilder();
        for (char ch : set) {
            alphabetS.append(ch);
        }
        ALPHABET = alphabetS.toString();
    }


    //main - Test
    public static void main(String[] args) {

        try {
            // Test abra wystepuje 3 razy w abracabradabra$
            String s = "abracabradabra$";
            String pattern = "abra";

            saveAlphabet(s);


            Node root = buildSuffixTree(s);

            // Jesli chcemy wydrukowac drzewo nalezy odkomentowac
            // printTree(root, s, 0);

            System.out.println(" ");
            System.out.println("Wzorzec p = "+ pattern+ " wystepuje w tekscie s = "+s
                    +"  "+patternSearch(root,pattern)+ " raz(y)");

            //koniec testu



            //Scanner do testow
            System.out.println("Sprawdz inne wzorce p w danym tekscie s");
            System.out.println("Podaj tekst s: ");

            Scanner tekst_s = new Scanner(System.in); //obiekt do odebrania danych od użytkownika
            s = tekst_s.nextLine()+"$";

            System.out.println("Podaj wzorzec p: ");
            Scanner wzorzec_p = new Scanner(System.in); //obiekt do odebrania danych od użytkownika
            pattern = wzorzec_p.nextLine();

            saveAlphabet(s);

            Node root_test = buildSuffixTree(s);

            // Jesli chcemy wydrukowac drzewo nalezy odkomentowac
            // printTree(root_test, s, 0);

            System.out.println(" ");
            System.out.println("Wzorzec p = "+ pattern+ " wystepuje w tekscie s = "+s
                    +"  "+patternSearch(root_test,pattern)+ " raz(y)");

        }
        catch(StringIndexOutOfBoundsException err){
            System.out.println("USTAW ALPHABET "+err);
        }

    }

}

