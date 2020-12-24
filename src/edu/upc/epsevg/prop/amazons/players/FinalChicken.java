/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import edu.upc.epsevg.prop.amazons.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Random;

/**
 *
 * @author Adrià Tort i Achraf Hmimou
 */
public class FinalChicken implements IPlayer, IAuto {

    String name;

    // profunditat
    int prof;
    boolean profunditatIterada;
    boolean timeout;

    // numero de nodes
    long nnodes;

    // taula de zobrist per representar al taulell
    // long[][][] zobristTable;

    /**
     * Constructor buit, en aquest cas la profundització es farà de forma
     * iterada.
     */
    public FinalChicken() {
        this.name = "FinalChicken";

        // Com que no s'ha pasat cap parametre la profunditat es realitzarà de forma iterada fins a un timeout.
        this.profunditatIterada = true;
        this.timeout = false;
        this.nnodes = 0;

        // inicialització i generació de la taula zobrist
        // this.zobristTable = new long[10][10][3];
        // generaTaulaHash();
    }

    /**
     * Constructor parametritzat amb una profunditat determinada que inhabilita el temporitzador (timeout).
     *
     * @param profunditat El valor de la profunditat haurà de ser > 0
     */
    public FinalChicken(int profunditat) {
        this.name = "FinalChicken";

        // En quest cas la profunditat serà fixa, no iterada.
        this.profunditatIterada = false;
        this.prof = profunditat;
        this.timeout = false;

        this.nnodes = 0;

        // inicialització i generació de la taula zobrist
        //this.zobristTable = new long[10][10][3];
        // generaTaulaHash();
    }

    /**
     * Decideix el moviment donat un estat del taulell.
     *
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        
        // Inicialitzem sempre el timeout a false per cada moviment.
        this.timeout = false;

        // en m tindrem el moviment actual i en mAnterior tindrem el moviment anterior en cas de que hi hagi timout i la variable m estigui a null.
        Move m = null;
        Move mAnterior = null;
        

        //En cas de que la profunditat sigui iterada
        if (profunditatIterada) {
            //comencarem amb una profunditat = 1
            this.prof = 1;
            // mentre no hi hagi timout
            while (!this.timeout) {

                // Ens guardaem sempre el moviment anterior
                if (m != null) {
                    mAnterior = new Move(m.getAmazonFrom(), m.getAmazonTo(), m.getArrowTo(), (int) m.getNumerOfNodesExplored(), m.getMaxDepthReached(), m.getSearchType());
                }

                //Calcularem el moviment actual donat l'estat i la profunditat fixada
                m = movimentProfunditat(s);

                //Augmentem la profunditat en una unitat
                ++this.prof;
            }
        } // En cas de que la profunditat sigui fixada
        else {
            // Retornem el millor moviment fins a la profunditat fixada al constructor.
            m = movimentProfunditat(s);
        }

        // En cas de que no s'hagi calculat correctament el moviment degut al timeout, retornarem el moviment anterior. Sino retornarem el moviment actual.
        if (m.getAmazonFrom() == null) {
            return mAnterior;
        } else {
            return m;
        }
    }

    /**
     * Funcio que retorna el millor moviment trobat fins a una profunditat
     * determinada donat un estat del taulell utilitzant l'algorisme minimax.
     *
     * El que fem en aquesta funció es preparar les podes alfa i beta amb els
     * seus valors i la profunidat a la que arribarem (com a màxim). Després
     * generarem totes les combinacions de moviments possibles i realitzarem el
     * minimax assegurant tenint sempre a la variable millorMoviment el moviment
     * més prometedor.
     *
     *
     * @param s estat determinat del taulell.
     * @return millor moviment fins a una profunditat determinada.
     */
    private Move movimentProfunditat(GameStatus s) {

        // reinicialitzzem cada vegada el numero de nodes que explorarem durant la crida del minimax.
        this.nnodes = 0;
        int Alpha = Integer.MIN_VALUE;
        int Beta = Integer.MAX_VALUE;

        // fixem la profunditat maxima que ens vindra donada desde el constructor parametritzat o de forma iterada desde la funcio Move.
        int profunditat = this.prof;

        // Aprofitem per calcular el valor del hash en el estat actual del taulell.
        //long hash = RecalcularHash(s);
        
        // Posarem el millor Moviment en una array on el primer element es el punt on es trobla l'amazona i el segon on mourem l'amazona i el tercer on colocarem la fletxa.
        Point[] millorMoviment = new Point[3];

        // També ens fa falta una array on guardarem els valors per cada moviment realitzat
        Point[] moviment = new Point[3];

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted == si es false revisa todas las posiciones, si es true solo las limite.
            for (int m = 0; m < MovimentsAmazona.size(); m++) { //Quitado timeout

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size(); c++) { //quitado timeout

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);
//
                    // Aprofitem per fer les diferents xors per obtenir el valor hash despres de haber fet el moviment complet.

//                    hash ^= this.zobristTable[origen.x][origen.y][s.getCurrentPlayer().ordinal() - 1];
//                    hash ^= this.zobristTable[desti.x][desti.y][s.getCurrentPlayer().ordinal() - 1];
//                    hash ^= this.zobristTable[fletxa.x][fletxa.y][CellType.ARROW.ordinal() - 1];
                    // Posarem el moviment en una array
                    moviment[0] = origen;
                    moviment[1] = desti;
                    moviment[2] = fletxa;

                    // Ara cridarem al minimitzador amb l'estat on hem fet el moviment
                    int eval = Minimitzador(statusCopia, profunditat - 1, Alpha, Beta);

                    // En cas de que sigui la millor heuristica trobada fins ara
                    if (eval > Alpha) {
                        // guardarem la columna com a millorMoviment fins que un altre doni millor heuristica
                        millorMoviment = moviment.clone();
                        // actualitzem Alfa amb la millor heurisitica que hem trobat de moment.
                        Alpha = eval;
                    }

                }
            }

        }

        // Aquest cas es dona quan tots els nodes explorats donan com heuristica -Infinit, per tant tots els moviments son perdedors i farem un moviments qualsevol perque ja hem perdut.
        if (millorMoviment[0] == null) {
            millorMoviment = moviment.clone();
        }

        // Retornem el objecte Move que també conte tota la informació obtinguda durant la exploracio.
        return new Move(millorMoviment[0], millorMoviment[1], millorMoviment[2], (int) this.nnodes, this.prof, SearchType.MINIMAX);
    }

    /**
     * Funció que s'encarrega d'establir la capa Maximitzadora on ha de retornar
     * el valor heuristic maxim obtingut d'evaluar tots els estats fills del
     * estat donat.
     *
     * @param s Estat determinat del taulell
     * @param Profunditat Profunditat maxima actual
     * @param Alpha Es tracta del valor que contindra el maxim heuristic actual
     * en les diferents crides.
     * @param Beta Es tracta del valor que contindra el minim exigible en les
     * diferents crides
     * @return retornarem la heuristica maxima obtinguda de evaluar als fills
     * del estat.
     */
    private int Maximitzador(GameStatus s, int Profunditat, int Alpha, int Beta) {

        // En cas de que en aquest estat el tinguem un guanyador i sugi el rival, hem perdut per tant retornem -infinit
        if (s.GetWinner() == CellType.opposite(s.getCurrentPlayer())) {
            return Integer.MIN_VALUE;
        }

        // Evaluem si en aquesta columna no es pot afegir mes fitxes o hem arribat a la profunditat maxima de exploracio. També mirarem si hi ha timout.
        if (Profunditat == 0 || this.timeout) {
            return -1 * getHeuristica(s, CellType.opposite(s.getCurrentPlayer()));                                 // Retornem la evaluacio heurisitca del taulell en aquesta situacio
        }

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);

                    // Cridarem a la evaluacio que fa el minimitzador amb una profunditat major.
                    int eval = Math.max(Alpha, Minimitzador(statusCopia, Profunditat - 1, Alpha, Beta));
                    Alpha = Math.max(Alpha, eval);
                    // Comprobem si beta es menor que Alpha, aixo indicara que per aquesta branca no cal seguir i retornem Beta com el minim exigible del arbre.
                    if (Beta <= Alpha) {
                        return Beta;
                    }
                }
            }
        }
        return Alpha;   // Retornem Alpha que sera el valor heuristic maxim obtingut del proces.
    }

    /**
     * Funcio que s'encarrega de establir la capa Minimitzadora don ha de
     * retornar el valor heuristic minim obtingut d'evaluar tots els estats
     * fills del estat donat.
     *
     * @param s Estat determinat del taulell
     * @param Profunditat Profunditat maxima actual
     * @param Alpha Es tracta del valor que contindra el maxim exigible
     * heuristic actual en les diferents crides.
     * @param Beta Es tracta del valor que contindra el minim en les diferents
     * crides
     * @return retornarem la heuristica minima obtinguda de evaluar als fills
     * del estat.
     */
    private int Minimitzador(GameStatus s, int Profunditat, int Alpha, int Beta) {

        // En cas de ser solucio, hem perdut. Vol dir que el ultim moviment (rival) ens ha guanyat.
        //  if(t.solucio(Columna, -color)) return Integer.MIN_VALUE;
        if (s.GetWinner() == CellType.opposite(s.getCurrentPlayer())) {
            return Integer.MAX_VALUE;
        }

        // Evaluem si en aquesta columna no es pot afegir mes fitxes o hem arribat a la profunditat maxima de exploracio
        // MIRAR SI HEMOS ACABADO EL TIMEOUT.
        if (Profunditat == 0 || this.timeout) {
            return getHeuristica(s, CellType.opposite(s.getCurrentPlayer()));                                 // Retornem la evaluacio heurisitca del taulell en aquesta situacio
        }

        // Per cada amazona del jugador
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor() && (!this.timeout); i++) {

            // Mirarem quins son els seus moviments possibles
            ArrayList<Point> MovimentsAmazona = s.getAmazonMoves(s.getAmazon(s.getCurrentPlayer(), i), false); // restricted ???
            for (int m = 0; m < MovimentsAmazona.size() && (!this.timeout); m++) {

                // Per cada moviment possible, mirarem quines son les caselles en las que podria posar una fletxa.
                LinkedList<Point> casellesBuides = casellasDisponibles(s, i, MovimentsAmazona.get(m));

                // Per cada casella on podem posar la fletxa, generarem un estat del taulell.
                for (int c = 0; c < casellesBuides.size() && (!this.timeout); c++) {

                    // Fem una copia del taulell on realitzarem els moviments.
                    GameStatus statusCopia = new GameStatus(s);

                    // Posicio on estroba la Amazona.
                    Point origen = s.getAmazon(s.getCurrentPlayer(), i);

                    // Posicio on mourem la Amazona.
                    Point desti = MovimentsAmazona.get(m);

                    // Posicio on posarem la fletxa.
                    Point fletxa = casellesBuides.get(c);

                    // Sobre l'estat copia del taulell que hem realitzem abans farem el moviment.
                    statusCopia.moveAmazon(origen, desti);
                    statusCopia.placeArrow(fletxa);

                    // Cridarem a la evaluacio que fa el maximitzador amb una profunditat major.
                    int eval = Math.min(Beta, Maximitzador(statusCopia, Profunditat - 1, Alpha, Beta));
                    Beta = Math.min(Beta, eval);
                    // Comprobem si beta es menor que Alpha, aixo indicara que per aquesta branca no cal seguir i retornem Alpha com el maxim exigible del arbre.
                    if (Beta <= Alpha) {
                        return Alpha;
                    }
                }
            }

        }
        return Beta;   // Retornem Beta que sera el valor heuristic minim obtingut del proces.
    }

    /**
     * Funció que ens retorna una llista amb totes les caselles buides del
     * taulell despres de moure una Amazona
     *
     * @param s Estat del taulell determinat
     * @param am Representa el integer que ens diu quina amazona sera
     * @param FIN Representa la posicio on mourem l'amazona.
     * @return retornem una llista amb totes les posicions buides del taulell on
     * potencialment podem posar una fletxa
     */
    private LinkedList<Point> casellasDisponibles(GameStatus s, int am, Point FIN) {

        LinkedList<Point> casellesBuides = new LinkedList<Point>();

        Point posicio;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                posicio = new Point(i, j);
                // Si es una peca buida i no es la posicio on mourem l'amazona sera considerada com a disponible.
                if ((s.getPos(i, j) == CellType.EMPTY) && (!posicio.equals(FIN))) {
                    casellesBuides.add(posicio);
                }
            }
        }
        // Finalment afegim la posicio inical de l'amazona abans de fer el moviment ja que una vegada fet el moviment quedara buida.
        casellesBuides.add(s.getAmazon(s.getCurrentPlayer(), am));
        return casellesBuides;
    }

     /**
     * Funció que envers un GameStatus i un Jugador identificat per el color 
     * retorna un enter (positiu o negatiu) d'una jugada especifica, on
     * es comproven tots els valors adjacents a cada peça i la movilitat d'aquestes.
     * 
     *
     * @param s Estat del taulell determinat
     * @param color Representa el color de les Amazones 
     * @return Un enter amb el valor de l'heurística
     * 
     */
    
    public int getHeuristica(GameStatus s, CellType color) {

//      Generem els valors inicials que farem servir per calcular cada jugada
//      i la final
        int movPossibles = 0;
        int movPossiblesRival = 0;
        int encontra = 0; int afavor = 0;
        
//      Generem els ArraysList i els punts per a calcular el nombre de moviments
        ArrayList<Point> MovimentsAmazona;
        ArrayList<Point> MovimentsAmazonaEnemigues;
        Point maleante = new Point();
        Point honorable = new Point();

        //Sabent que hi ha el mateix nombre de blanques que de negres, seleccionem
        //creem un comptador que vagi desde 0 fins al nombre de Amazones que te
        //el tauler
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor(); i++) {
            //Aconseguim les amazones del (color) i nombre (i) indicades
            MovimentsAmazona = s.getAmazonMoves(s.getAmazon(color, i), false);
            
            //Sumem la mida del Array a la variable "movPossibles"
            movPossibles += MovimentsAmazona.size();
            
            //Aconseguim les amazones del (color) i enter (i) indicades
            MovimentsAmazonaEnemigues = s.getAmazonMoves(s.getAmazon(CellType.opposite(color), i), false); // restricted
            
            //Sumem la mida del Array a la variable "movPossiblesRival"
            movPossiblesRival += MovimentsAmazonaEnemigues.size();
            
            //Inicialitzem les variables "encontra" i "afavor".
            encontra = 0;
            afavor = 0;

            //Inicialitzem els punters "maleante" i "honorable" amb el Jugador 
            //(color) i el enter (i) indicat.
            //maleante correspon al equip enemic en aquesta heurística i
            //honorable al nostre equip.
            maleante = new Point(s.getAmazon(CellType.opposite(color), i));
            honorable = new Point(s.getAmazon(color, i));

            //Mirarem totes les posicions adjacents a aquestes fitxes en busca
            //de posibles problemes o ventatges al seu voltant (sols a una 
            //posició de distancia).
            for (int x = -1; x < 2; x++){
                for (int y = -1; y<2; y++){
                    
                    //MIRANT L'ENEMIC: Si la posició que anem a mirar no 
                    //sobresurt dels limits i aquesta conté una CREUETA o una
                    //fitxa del JUGADOR NOSTRE; Llavors sumem 1 a la variable
                    //que juga al nostre favor (afavor).
                    if(maleante.x+x>=0 && maleante.x+x<s.getSize() && maleante.y+y>=0 && maleante.y+y<s.getSize() && ((s.getPos(maleante.x+x,maleante.y+y) == CellType.ARROW) || (s.getPos(maleante.x+x,maleante.y+y) == color))){
                        ++afavor;
                    }
                    
                    //MIRANT L'AMIC: Si la posició que anem a mirar no 
                    //sobresurt dels limits i aquesta conté una CREUETA o una
                    //fitxa del JUGADOR CONTRARI; Llavors sumem 1 a la variable
                    //que juga al nostre favor (afavor).
                    if(honorable.x+x>=0 && honorable.x+x<s.getSize() && honorable.y+y>=0
                    && honorable.y+y<s.getSize() &&
                    ((s.getPos(honorable.x+x,honorable.y+y) == CellType.ARROW) ||
                    (s.getPos(honorable.x+x,honorable.y+y) == CellType.opposite(color)))){
                        ++encontra;
                    }

                }
            }
            
            //Creem un boolea paret que ens indicarà quan una fitxa resideix
            //a la vora del mur. Quan descobrim que resideix aqui la posarem
            //sota vigilancia i si torna a apareixer voldrá dir que viu en una
            //cantonada i per tant mentres de sumarli 3 n'hi sumarem 2.
            boolean paret = false;
            
            //Comprovem la fitxa enemiga en X equivalent a la pos. 0
            if(maleante.x == 0){
                afavor +=3;
                paret = true;
            }
            
            //Comprovem la fitxa enemiga en X equivalent a la pos. MIDA-1
            if(maleante.x == s.getSize()-1){
                if(paret){
                    afavor+=2;
                }else{
                    afavor +=3;
                    paret = true;
                }

            }

            //Comprovem la fitxa enemiga en Y equivalent a la pos. 0
            if(maleante.y == 0){
                if(paret){
                    afavor+=2;
                }else{
                    afavor +=3;
                    paret = true;
                }
            }
            
            //Comprovem la fitxa enemiga en Y equivalent a la pos. MIDA-1
            if(maleante.y == s.getSize()-1){
                if(paret){
                    afavor+=2;
                }else{
                    afavor +=3;
                    //No modifiquem paret ja que la reinicialitzarem.
                }
            }
//------------------------------------------------------------------------------
            paret = false;

            //Comprovem la fitxa enemiga en X equivalent a la pos. 0
            if(honorable.x == 0){
                encontra += 3;
                paret = true;
            }
            
            //Comprovem la fitxa enemiga en X equivalent a la pos. MIDA-1
            if(honorable.x == s.getSize()-1){
                if(paret){
                    encontra+=2;
                }else{
                    encontra +=3;
                    paret = true;
                }
            }
            
            //Comprovem la fitxa enemiga en Y equivalent a la pos. 0
            if(honorable.y == 0){
                if(paret){
                    encontra+=2;
                }else{
                    encontra +=3;
                    paret = true;
                }
            }
            
            //Comprovem la fitxa enemiga en Y equivalent a la pos. MIDA-1
            if(honorable.y == s.getSize()-1){
                if(paret){
                    encontra+=2;
                }else{
                    encontra +=3;
                    //No modifiquem paret ja que la reinicialitzarem.
                }
            }
            
            //Finalment elevem a 2 el resultat corresponent i el sumem al
            //resultat final, les variables "afavor" i "encontra" es reinicialitzarán.
            movPossibles += Math.pow(2, afavor);
            movPossiblesRival += Math.pow(2, encontra);
        }
        
        //Sumem 1 al nnodes en vers que hem explorat una heuristica més
        ++this.nnodes;
        
        //Retornem la resta dels nostres moviments envers les del enemic.
        //El valor ens ha de servir per discriminar on posar la creueta i la
        //fitxa en la seguent iteració.
        return (movPossibles - movPossiblesRival);
    }

//    private int evaluaTerritori(GameStatus s, ArrayList<ArrayList<Point>> taulaTerritori, Point Amazona, int nMovimentsPerPosicio, int taulellCompleta, int nElementTerritori) {
//
//
//        while (nElementTerritori < taulellCompleta) {
//
//            if (nMovimentsPerPosicio == 1) {
//                ArrayList<Point> moviments = s.getAmazonMoves(Amazona, false);
//                taulaTerritori.add(moviments);
//                nElementTerritori += moviments.size();
//                if(moviments.size() == 0)
//                    break;
//            }
//
//            else if (nMovimentsPerPosicio > 1) {
//                taulaTerritori.add(new ArrayList());
//                for (int i = 0; i < taulaTerritori.get(nMovimentsPerPosicio - 2).size() && nElementTerritori < taulellCompleta; i++) {
//                    Point posicio = taulaTerritori.get(nMovimentsPerPosicio - 2).get(i);
//                    ArrayList<Point> moviments = obtenirMovimentsDisponibles(s, posicio);
//
//                    for (int m = 0; m < moviments.size() && nElementTerritori < taulellCompleta; m++) {
//                        if (!taulaTerritori.get(nMovimentsPerPosicio - 2).contains(moviments.get(m))) {
//                            taulaTerritori.get(nMovimentsPerPosicio - 1).add(moviments.get(m));
//                            nElementTerritori++;
//                        }
//                    }
//                }
//            }
//
//           // System.out.println("nElementTErriotri"+nElementTerritori);
//
//            ++nMovimentsPerPosicio;
//        }
//
//        int evaluacio = 0;
//
//        for(int e = 1; e <= taulaTerritori.size(); e++)
//        {
//            evaluacio += e * taulaTerritori.get(e-1).size();
//        }
//        return evaluacio;
//    }

    private ArrayList<Point> obtenirMovimentsDisponibles(GameStatus s, Point Posicio)
    {
        ArrayList<Point> MovimentsDisponibles = new ArrayList<Point>();

        ArrayList<Point> dir = new ArrayList<Point>();

        for (int i = -1; i < 2; i++)
        {
            for(int j = -1; j<2; j++){
                if(!(i == 0 && j == 0))
                    dir.add(new Point(i,j));
            }
        }

        for(int i = 0; i<dir.size(); i++)
        {
           int posX = Posicio.x + dir.get(i).x;
           int posY = Posicio.y + dir.get(i).y;

           while((posX>0 && posX<s.getSize()) && (posY>0 && posY<s.getSize()) && s.getPos(posX, posY) == CellType.EMPTY)
           {
               MovimentsDisponibles.add(new Point(posX, posY));
               posX+=dir.get(i).x;
               posY+=dir.get(i).y;

           }
        }
       return MovimentsDisponibles;
    }




    /**
     * Funció que s'encarrega de generar la taula de Zobrist que representarà
     * totes les configuracions del taulell.
     *
     */
//    private void generaTaulaHash() {
//
//        Random rand = new Random();
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 10; j++) {
//                for (int k = 0; k < 3; k++) {
//                    this.zobristTable[i][j][k] = rand.nextLong();
//                }
//            }
//        }
//    }

//    /**
//     * Funció que itera sobre tot el taulell i retorna el valor hash que
//     * representa l'estat del tauell.
//     *
//     * @param s Estat determinat del taulell
//     * @return Retorna el valor hash obtingut que representa el estat del
//     * taulell.
//     */
//    private long RecalcularHash(GameStatus s) {
//        long h = 0;
//        //TODO: Hacer variable el tamaño del tablero
//        for (int i = 0; i < 9; i++) {
//            for (int j = 0; j < 9; j++) {
//                // Anirem fent acomuladament ela operació xor entre totes les peces no buides del tauell.
//                if (s.getPos(i, j) != CellType.EMPTY) {
//                    CellType pieza = s.getPos(i, j);
//                    h ^= this.zobristTable[i][j][pieza.ordinal() - 1];
//                }
//            }
//        }
//        return h;
//    }

    /**
     * Funció que ens avisa que hem de parar la cerca en curs perquè s'ha
     * exhaurit el temps de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !

        // En cas de que la profunditat sigui iterada activarem el timeout.
        if (profunditatIterada) {
            this.timeout = true;
        }
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return name;
    }
}
