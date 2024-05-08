package R2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DieSiedler {
    public static void main(String[] args) {
        DieSiedler siedler = new DieSiedler();

        System.out.println("Weg (Path) zur .txt angeben. Beispiel: .../siedler/siedler3.txt");
        Scanner scanner = new Scanner(System.in);
        siedler.erstelle_plan(siedler.einlesen(scanner.nextLine()));
    }

    void erstelle_plan(List<Punkt> polygon) {
        polygon.add(polygon.get(0)); //Polygon schließen


        List<Kreis> besterPlan = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Kreis> plan;
            plan = loese(polygon, 0);

            if(plan.size() > besterPlan.size())
                besterPlan = plan;
        }


        System.out.println("Positionen der Ortschaften:");
        System.out.println(" ");
        for (Kreis ortschaft : besterPlan) {
            System.out.println(ortschaft.mPunkt.x + " " + ortschaft.mPunkt.y);
        }

        System.out.println(" ");
        System.out.println("Anzahl der Ortschaften: " + besterPlan.size());

        MyFrame frame = new MyFrame(polygon, besterPlan);
    }

    List<Kreis> loese(List<Punkt> polygon, int strategie) {

        List<Kreis> ortschaften_GZ = GZ_planen(polygon);
        List<Kreis> ortschaften_rand = rand_planen(ortschaften_GZ, polygon);
        List<Kreis> ortschaften_innen = new ArrayList<>();

        switch (strategie) {
            case 0 -> {
                ortschaften_innen = innen_planen_vorwaerts(ortschaften_rand, ortschaften_GZ, polygon, 360);
            }
            case 1 -> {
                ortschaften_innen = innen_planen_rueckwaerts(ortschaften_rand, ortschaften_GZ, polygon, 360);
            }
            case 2 -> {
                ortschaften_innen = innen_planen_vorwaerts(ortschaften_rand, ortschaften_GZ, polygon, 180);
            }
            case 3 -> {
                ortschaften_innen = innen_planen_rueckwaerts(ortschaften_rand, ortschaften_GZ, polygon, 180);
            }
            case 4 -> {
                ortschaften_innen = innen_planen_90(ortschaften_rand, ortschaften_GZ, polygon);
            }
        }

        List<Kreis> ortschaften = new ArrayList<>(ortschaften_GZ);
        ortschaften.addAll(ortschaften_rand);
        ortschaften.addAll(ortschaften_innen);

        return ortschaften;
    }

    List<Kreis> GZ_planen(List<Punkt> polygon) {
        List<Kreis> ortschaften_GZ = new ArrayList<>();

        for (double radius = 85; radius > 0; radius -= 10) {
            double winkel_grad = 0;

            while (winkel_grad < 360) {
                double winkel_bogenmasz = (winkel_grad * Math.PI) / 180;
                double x = radius * Math.cos(winkel_bogenmasz);
                double y = radius * Math.sin(winkel_bogenmasz);

                Kreis naechsterKreis = new Kreis(new Punkt(x, y), 5);
                naechsterKreis.imGZ = true;

                if(!ueberschneidet(naechsterKreis, ortschaften_GZ)) {
                    ortschaften_GZ.add(naechsterKreis);
                }

                winkel_grad += 0.01;
            }
        }


        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Punkt punkt : polygon) {
            if(punkt.x < minX)
                minX = punkt.x;
            if(punkt.y < minY)
                minY = punkt.y;
            if(punkt.x > maxX)
                maxX = punkt.x;
            if(punkt.y > maxY)
                maxY = punkt.y;
        }

        Punkt aktuellerPunkt = new Punkt(minX, minY);
        List<Kreis> beste_ortschaften_GZ_im_polygon = new ArrayList<>();

        while (aktuellerPunkt.y < maxY) {
            while (aktuellerPunkt.x < maxX) {
                List<Kreis> ortschaften_GZ_im_polygon = new ArrayList<>();

                if(imPolygon(aktuellerPunkt, polygon)) {

                    for (Kreis ortschaft : ortschaften_GZ) {
                        Kreis naechsteOrtschaft = new Kreis(new Punkt(ortschaft.mPunkt.x+aktuellerPunkt.x, ortschaft.mPunkt.y+ aktuellerPunkt.y), 5);
                        if(imPolygon(new Punkt(naechsteOrtschaft.mPunkt.x, naechsteOrtschaft.mPunkt.y), polygon)) {
                            ortschaften_GZ_im_polygon.add(naechsteOrtschaft);
                        }
                    }

                    if(ortschaften_GZ_im_polygon.size() > beste_ortschaften_GZ_im_polygon.size()) {
                        beste_ortschaften_GZ_im_polygon = new ArrayList<>(ortschaften_GZ_im_polygon);
                    }
                }
                aktuellerPunkt.x++;
            }
            aktuellerPunkt.x = minX;
            aktuellerPunkt.y+=1;
        }

        return beste_ortschaften_GZ_im_polygon;
    }

    boolean ueberschneidet(Kreis aktuelleOrtschaft, List<Kreis> ortschaften) {
        for(Kreis ortschaft : ortschaften) {
            if(ortschaft.imGZ && aktuelleOrtschaft.imGZ) {
                if( getAbstand(ortschaft.mPunkt, aktuelleOrtschaft.mPunkt) < 9.999999) {
                    return true;
                }
            }
            if(ortschaft.imGZ && !aktuelleOrtschaft.imGZ || !ortschaft.imGZ && aktuelleOrtschaft.imGZ) {
                if( getAbstand(ortschaft.mPunkt, aktuelleOrtschaft.mPunkt) < 14.999999)
                    return true;
            }
            if(!ortschaft.imGZ && !aktuelleOrtschaft.imGZ) {
                if( getAbstand(ortschaft.mPunkt, aktuelleOrtschaft.mPunkt) < 19.999999)
                    return true;
            }
        }

        return false;
    }

    List<Kreis> innen_planen_vorwaerts(List<Kreis> ortschaften_rand, List<Kreis> ortschaften_GZ, List<Punkt> polygon, int ansetzPunkt) {
        List<Kreis> aktuelleSchicht = new ArrayList<>(ortschaften_rand);
        List<Kreis> naechsteSchicht = new ArrayList<>();

        List<Kreis> ortschaften_innen = new ArrayList<>();
        List<Kreis> ortschaften = new ArrayList<>();
        ortschaften.addAll(ortschaften_rand);
        ortschaften.addAll(ortschaften_GZ);

        boolean gefunden = true;

        while (gefunden){
            gefunden = false;

            for (int i = 0; i < aktuelleSchicht.size(); i++) {
                int winkel_grad = ansetzPunkt;

                while (winkel_grad < ansetzPunkt + 360) {
                    double winkel_bogenmasz = (winkel_grad * Math.PI) / 180;
                    double x = 20 * Math.cos(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.x;
                    double y = 20 * Math.sin(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.y;

                    Kreis kreis = new Kreis(new Punkt(x, y), 10);

                    if(imPolygon(kreis.mPunkt, polygon) && !ueberschneidet(kreis, ortschaften)) {
                        ortschaften.add(kreis);
                        naechsteSchicht.add(kreis);
                        ortschaften_innen.add(kreis);
                        gefunden = true;
                    }
                    winkel_grad += 1;
                }
            }

            aktuelleSchicht = new ArrayList<>(naechsteSchicht);
        }

        return ortschaften_innen;
    }

    List<Kreis> innen_planen_rueckwaerts(List<Kreis> ortschaften_rand, List<Kreis> ortschaften_GZ, List<Punkt> polygon, int ansetzPunkt) {
        List<Kreis> aktuelleSchicht = new ArrayList<>(ortschaften_rand);
        List<Kreis> naechsteSchicht = new ArrayList<>();

        List<Kreis> ortschaften_innen = new ArrayList<>();
        List<Kreis> ortschaften = new ArrayList<>();
        ortschaften.addAll(ortschaften_rand);
        ortschaften.addAll(ortschaften_GZ);
        boolean gefunden = true;

        while (gefunden){
            gefunden = false;

            for (int i = 0; i < aktuelleSchicht.size(); i++) {
                int winkel_grad = ansetzPunkt;

                while (winkel_grad > ansetzPunkt - 360) {
                    double winkel_bogenmasz = (winkel_grad * Math.PI) / 180;
                    double x = 20 * Math.cos(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.x;
                    double y = 20 * Math.sin(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.y;

                    Kreis kreis = new Kreis(new Punkt(x, y), 10);

                    if(imPolygon(kreis.mPunkt, polygon) && !ueberschneidet(kreis, ortschaften)) {
                        ortschaften.add(kreis);
                        naechsteSchicht.add(kreis);
                        ortschaften_innen.add(kreis);
                        gefunden = true;
                    }
                    winkel_grad -= 1;
                }
            }

            aktuelleSchicht = new ArrayList<>(naechsteSchicht);
        }

        return ortschaften_innen;
    }

    List<Kreis> innen_planen_90(List<Kreis> ortschaften_rand, List<Kreis> ortschaften_GZ, List<Punkt> polygon) {
        List<Kreis> aktuelleSchicht = new ArrayList<>(ortschaften_rand);
        List<Kreis> naechsteSchicht = new ArrayList<>();

        List<Kreis> ortschaften_innen = new ArrayList<>();
        List<Kreis> ortschaften = new ArrayList<>();
        ortschaften.addAll(ortschaften_rand);
        ortschaften.addAll(ortschaften_GZ);

        boolean gefunden = true;

        while (gefunden){
            gefunden = false;

            for (int i = 0; i < aktuelleSchicht.size(); i++) {
                int winkel_grad = 0;

                while (winkel_grad < 360) {
                    double winkel_bogenmasz = (winkel_grad * Math.PI) / 180;
                    double x = 20 * Math.cos(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.x;
                    double y = 20 * Math.sin(winkel_bogenmasz) + aktuelleSchicht.get(i).mPunkt.y;

                    Kreis kreis = new Kreis(new Punkt(x, y), 10);

                    if(imPolygon(kreis.mPunkt, polygon) && !ueberschneidet(kreis, ortschaften)) {
                        ortschaften.add(kreis);
                        naechsteSchicht.add(kreis);
                        ortschaften_innen.add(kreis);
                        gefunden = true;
                    }
                    winkel_grad += 90;
                }

            }

            aktuelleSchicht = new ArrayList<>(naechsteSchicht);
        }

        return ortschaften_innen;
    }

    List<Kreis> rand_planen(List<Kreis> ortschaften_GZ, List<Punkt> polygon) {

        List<Kreis> ortschaften_rand = new ArrayList<>();
        List<Kreis> ortschaften = new ArrayList<>(ortschaften_GZ);

        for (int i = 0; i < polygon.size() - 1; i++) {
            Punkt A = polygon.get(i);
            Punkt B = polygon.get(i + 1);

            if (A.x > B.x) {
                Punkt temp = B;
                B = A;
                A = temp;
            }

            double winkel_bogenmasz = Math.atan((B.y - A.y) / (B.x - A.x));

            double cos_wert = Math.cos(winkel_bogenmasz);
            double sin_wert = Math.sin(winkel_bogenmasz);

            Kreis ortschaftA = new Kreis(A, 10);
            if (!ueberschneidet(ortschaftA, ortschaften)) {
                ortschaften_rand.add(ortschaftA);
            }

            Kreis ortschaftB = new Kreis(B, 10);
            if (!ueberschneidet(ortschaftB, ortschaften)) {
                ortschaften_rand.add(ortschaftB);
            }

            double vielfach = 0.0; //In der Dokumentation als i beschrieben
            Punkt aktuellerPunkt = A;
            double verbleibendeLaenge = getAbstand(A, B) - 2 * 10;

            while (verbleibendeLaenge > 0) {
                Punkt naechsterPunkt = new Punkt(vielfach * 2 * 10 * cos_wert + aktuellerPunkt.x,  vielfach * 2 * 10 * sin_wert + aktuellerPunkt.y);

                if (ueberschneidet(new Kreis(naechsterPunkt, 10), ortschaften)) {
                    vielfach += 0.25;
                    continue;
                }

                aktuellerPunkt = naechsterPunkt;

                verbleibendeLaenge -= vielfach * 2 * 10;

                if (verbleibendeLaenge > 0) {
                    Kreis ortschaft = new Kreis(aktuellerPunkt, 10);
                    ortschaften_rand.add(ortschaft);
                    ortschaften.add(ortschaft);
                }

                vielfach = 1;
            }

        }

        return ortschaften_rand;
    }

    boolean imPolygon(Punkt P, List<Punkt> polygon) {
        int zaehler = 0;
        for (int i = 0; i < polygon.size()-1; i++) {
            if(schneidetKante(polygon.get(i), polygon.get(i + 1), P)) {
                zaehler++;
            }
        }

        if(zaehler % 2 == 0) {
            return false;
        }
        return true;
    }

    boolean schneidetKante(Punkt A, Punkt B, Punkt P) {
        if(A.y > B.y) {
            return schneidetKante(B, A, P);
        }

        if (P.y == A.y || P.y == B.y) {
            P.y += 0.000001;
        }

        if(P.x >= Math.max(A.x, B.x) || P.y > B.y || P.y < A.y )  {
            return false;
        }

        if(P.x < Math.min(A.x, B.x)) {
            return true;
        }

        double m_AB = (B.y - A.y) / (B.x - A.x);
        double m_AP = (P.y - A.y) / (P.x - A.x);

        if(m_AB >= m_AP) {
            return false;
        }

        return true;
    }

    double getAbstand(Punkt punkt1, Punkt punkt2) {
        return Math.sqrt(Math.pow((punkt2.x - punkt1.x), 2) + Math.pow((punkt2.y - punkt1.y), 2));
    }

    List<Punkt> einlesen(String datei_weg) {
        try{
            File file = new File(datei_weg);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<Punkt> polygon = new ArrayList<>();

            int n = Integer.valueOf(reader.readLine());
            for (int i = 0; i < n; i++) {
                int pos = 0;

                String zeile = reader.readLine();

                String x = "";

                while(zeile.charAt(pos) != ' ') {
                    x += zeile.charAt(pos);
                    pos++;
                }

                String y = "";

                while(pos != zeile.length()) {
                    y += zeile.charAt(pos);
                    pos++;
                }

                polygon.add(new Punkt(Double.valueOf(x), Double.valueOf(y)));
            }

            return polygon;

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    static class Punkt {
        double x;
        double y;
        public Punkt(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Kreis {
        Punkt mPunkt;
        double radius;
        boolean imGZ = false;

        public Kreis(Punkt punkt, double radius) {
            this.mPunkt = punkt;
            this.radius = radius;
        }
    }

}