/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Maha G.
 */
 /*public class InputData {

          //distance between nodes
                                                             //   0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6 7  8  9  0  1  2  3  4  5  6  7  8
                                                             //   S  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z AA BB CC DD EE FF GG HH II JJ KK LL
         final double[][] distanceMatrix = new double[][]  {{0, 4, 3, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // S
 								{4, 0, 0, 5, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // A
 								{3, 0, 0, 4, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // B
 								{6, 5, 4, 0, 2, 5, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // C
 								{0, 3, 0, 2, 0, 0, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // D
 								{0, 0, 7, 5, 0, 0, 1, 0, 2, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // E
 								{0, 0, 0, 2, 2, 1, 0, 2, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // F
 								{0, 0, 0, 0, 4, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // G
 								{0, 0, 0, 0, 0, 2, 5, 2, 0, 3, 4, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // H
                                                                 {0, 0, 0, 0, 0, 6, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // I
                                                                 {0, 0, 0, 0, 0, 0, 0, 2, 4, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // J
 								{0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 2, 0, 3, 2, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // K
 								{0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 3, 0, 0, 4, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0}, // L
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 0, 0, 4, 0, 4, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0}, // M
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 4, 4, 0, 3, 4, 2, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // N
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 3, 0, 0, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // O
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4, 6, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Q
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // R
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 0, 1, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // S
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // T
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // U
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // V
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // W
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // X
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 1, 1, 0, 1, 3, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Y
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0}, // Z
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // AA
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0}, // BB
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 1, 0, 3, 1, 2, 0, 0, 0, 0, 0, 0}, // CC
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 4, 0, 0, 0, 0, 0}, // DD
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 5, 2, 0, 0, 0}, // EE
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 5, 3, 0, 0, 0, 0}, // FF
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 1, 0, 1, 0, 0}, // GG
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 3, 1, 0, 1, 2, 1, 2}, // HH
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 3}, // II
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 2, 0}, // JJ
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 2}, // KK
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 2, 0}, // LL
 								};
       
     // index of the speed functions between two nodes
                                                             //   0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6 7  8  9  0  1  2  3  4  5  6  7  8
                                                             //   S  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  SS  T  U  V  W  X  Y  Z AA BB CC DD EE FF GG HH II JJ KK LL
         final int[][] speedFunctionMatrix = new int[][] {{0, 1, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // S 0
                                                                 {1, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // A 1
 								{3, 0, 0, 4, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // B 2
 								{2, 2, 4, 0, 2, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // C 3
 								{0, 3, 0, 2, 0, 0, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // D 4
 								{0, 0, 1, 4, 0, 0, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // E 5
 								{0, 0, 0, 2, 2, 1, 0, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // F 6
 								{0, 0, 0, 0, 4, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // G 7
 								{0, 0, 0, 0, 0, 2, 4, 2, 0, 3, 4, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0}, // H 8
                                                                 {0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // I 9
                                                                 {0, 0, 0, 0, 0, 0, 0, 2, 4, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // J 10
 								{0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 2, 0, 3, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // K 11
 								{0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 3, 0, 0, 4, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // L 12
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 0, 0, 4, 0, 4, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // M 13
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4, 4, 0, 3, 4, 2, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // N 14
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // O 15
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P 16
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4, 3, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Q 17
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // R 18
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 0, 1, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // SS 19
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // T 20
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // U 21
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // V 22
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // W 23
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // X 24
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 1, 1, 0, 1, 3, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Y 25
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0}, // Z 26
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // AA 27
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0}, // BB 28
 								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 1, 0, 3, 1, 2, 0, 0, 0, 0, 0, 0}, // CC 29
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 4, 0, 0, 0, 0, 0}, // DD 30
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 5, 2, 0, 0, 0}, // EE 31
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 5, 3, 0, 0, 0, 0}, // FF 32
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 1, 0, 1, 0, 0}, // GG 33
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 3, 1, 0, 1, 2, 1, 2}, // HH 34
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 0, 3}, // II 35
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 2, 0}, // JJ 36
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 2}, // KK 37
                                                                 {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 2, 0}, // LL 38
 								};
        // vehicle(i,j) i : vehicle identifier    j : vehicle load capacity
//         static final ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>() {{
//          add(new Vehicle(1,50));
//          add(new Vehicle(2,50));
//          add(new Vehicle(3,50));
//          add(new Vehicle(4,50));
//          add(new Vehicle(5,50));
//          add(new Vehicle(6,50));
//          add(new Vehicle(7,50));
//          add(new Vehicle(8,50));
//          add(new Vehicle(9,50));
//         }};
    final int nbVehicles = 9;
    //final int vehicleCapacity = 50;

    final Request[] requests = new Request[] {
            new Request(5, 1, 0.4, 1.3, 1.0, 2.0) };
    
         // customer(a,b,c,d,e,f) a : customer identifier b : customer name c : position in matrixSpeed
         //  d : load  e : minimum time requested by the customer   f : maximum time requested by the customer
         // customer(a,0)  depot a : name   0 : depot identifier
//         static final ArrayList<Customer> customerList = new ArrayList<Customer>() {{
//          add(new Customer("depot",0));
//          add(new Customer(1,"client 1",1,5,1,2)); //A
//          add(new Customer(2,"client 2",5,10,0.4f,1.3f)); //E
//          add(new Customer(3,"client 3",8,5,0,4));
//          add(new Customer(4,"client 4",11,5,0,8));
//          add(new Customer(5,"client 5",24,8,0,11));
//          add(new Customer(6,"client 6",29,9,0,11));
//          add(new Customer(7,"client 7",6,2,0,11));
//          add(new Customer(8,"client 8",10,5,0,11));
//          add(new Customer(9,"client 9",15,4,0,11));
//          add(new Customer(10,"client 10",3,5,0,3));
//          add(new Customer(11,"client 11",21,5,0,6));
//          add(new Customer(12,"client 12",22,20,0,8));
//          add(new Customer(13,"client 13",12,45,0,9));
//          add(new Customer(14,"client 14",19,20,0,11));
//          add(new Customer(15,"client 15",2,15,0,11));
//          add(new Customer(16,"client 16",20,25,0,11));
//          add(new Customer(17,"client 17",13,20,0,7));
//          add(new Customer(18,"client 18",18,15,0,10));
//          add(new Customer(19,"client 19",14,20,0,10));
//          add(new Customer(20,"client 20",17,20,0,10));
//
//         }};
  
        
         // node Name
//         static final String[] nodeName = new String []{ "S", "A",  "B",  "C",  "D",  "E",  "F",  "G",  "H",  "I",  "J",
//                                              "K",  "L",  "M",  "N",  "O",  "P",  "Q", "R", "SS", "T", "U", "V", "W", "X",
//                                              "Y", "Z", "AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH", "II", "JJ", "KK", "LL"};
//         static String[] nodeName;
//
//         static public void nodeRename(){
//             nodeName = new String[distanceMatrix.length];
//             for(int i=0; i < distanceMatrix.length; i++){
//                 nodeName[i] ="Node"+i;
//                 for(int j=0; j < customerList.size(); j++){
//                     if(i == customerList.get(j).distanceMatrixPosition){
//                         nodeName[i] = nodeName[i]+"--"+customerList.get(j).customerName;
//                     }
//                 }
//             }
//         }
              
      // route departure time
        //final double routeDepartureTime = 0;
      
       // speed functions
        final ArrayList<double[][]> speedFunctionList = new ArrayList<>(){{
			add(new double[][] {{1.0,10.0},{2.0,60.0},{3.0,25.0},{24.0,15.0}});
			add(new double[][] {{1.0,60.0},{24.0,30.0}});
			add(new double[][] {{6.0,70.0},{10.0,20},{15.0,70.0},{19.0,10.0},{24.0,70.0}});
			add(new double[][] {{6.0,100.0},{9.0,30.0},{14.0,80.0},{19.0,30.0},{24.0,90.0}});
		    add(new double[][] {{24.0,70.0}});
		    add(new double[][] {{24.0,70.0}});
        }};
    
     // different departure times used to generate the pool of possible routes between each pair of customers
     final double[] proposedDepartTime = {0.0, 4.0, 8.0, 12.0, 16.0, 20.0, 24.0};
     // proposed arrival time
     //final double proposedArrivalTime = 11.5f;
   
     // number of iterations
     final int iterationNbr = 1000;
     // number of iteration to reselect vehicle couple
     final int reselectedIterationNbr = 10;
    
 }*/

public class InputData {
 	//static final int depotIndex = 0;
    public final double[][] distanceMatrix;
    public final int[][] speedFunctionMatrix;
    public final ArrayList<Request> requests;
	public final ArrayList<double[][]> speedFunctionList;
	public final ArrivalTimeFunction[][] arcArrivalTimeFunctions;
    public final double[] proposedDepartTime;
    public final double depotTimeWindowUpperBound;
    public final int nbVehicles;

    public InputData(int nbNodes, int nbClients, double corr, int index, String tw) throws FileNotFoundException {
		distanceMatrix = new double[nbNodes][nbNodes];
		speedFunctionMatrix = new int[nbNodes][nbNodes];
		String[] line;

		Scanner sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/distances/LL-%d_%d_corr%.2f_%d.d",
				nbNodes, nbClients, corr, index)));

		// The first line is not important
		sc.nextLine();
		while(sc.hasNextLine())
		{
			line = sc.nextLine().trim().split("\\s+");
		    distanceMatrix[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
		}
		sc.close();

		double[][] travelTimes = new double[nbNodes][nbNodes];

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/travelTimes/LL-%d_%d_corr%.2f_%d.t",
				nbNodes, nbClients, corr, index)));

		// The first line is not important
		sc.nextLine();
		while(sc.hasNextLine()) {
		    line = sc.nextLine().trim().split("\\s+");
		    travelTimes[Integer.parseInt(line[0])][Integer.parseInt(line[1])] = Double.parseDouble(line[2]);
		}
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/instances/LL-%d_%d_corr%.2f_%d_%s.txt",
				nbNodes, nbClients, corr, index, tw)));

		line = sc.nextLine().trim().split("\\s+");

		nbVehicles = Integer.parseInt(line[1]);
		//int  capac  = Integer.parseInt(line[2]);

		line = sc.nextLine().trim().split("\\s+");
		assert Integer.parseInt(line[0]) == 0;
		depotTimeWindowUpperBound = Double.parseDouble(line[5]);

		requests = new ArrayList<>(nbClients / 2);

		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
			if (!sc.hasNextLine()) {
				break;
			}

			//double  dem =Float.parseFloat(temp[3]);
			int node1 = Integer.parseInt(line[0]);
			double timeWindowLowerBound1 = Double.parseDouble(line[4]);
			double timeWindowUpperBound1 = Double.parseDouble(line[5]);

			line = sc.nextLine().trim().split("\\s+");

			int node2 = Integer.parseInt(line[0]);
			double timeWindowLowerBound2 = Double.parseDouble(line[4]);
			double timeWindowUpperBound2 = Double.parseDouble(line[5]);

			// Select the earliest time window end (or start in case of equality) as the pickup point
			if (timeWindowUpperBound1 < timeWindowUpperBound2 ||
					timeWindowUpperBound1 == timeWindowUpperBound2 && timeWindowLowerBound1 <= timeWindowLowerBound2) {
				requests.add(new Request(node1, node2,
						timeWindowLowerBound1, timeWindowUpperBound1, timeWindowLowerBound2, timeWindowUpperBound2));
			} else {
				requests.add(new Request(node2, node1,
						timeWindowLowerBound2, timeWindowUpperBound2, timeWindowLowerBound1, timeWindowUpperBound1));
			}
		}
		sc.close();

		sc = new Scanner(new File(String.format("LL-instances_TDVRPTW/arcTypes/LL-%d_%d.txt", nbNodes, index)));
	    
		line = sc.nextLine().trim().split("\\s+");

		int nbIntervals = Integer.parseInt(line[0]);
		int nbTypes = Integer.parseInt(line[1]);
		double[][] speedFactors = new double[nbTypes][nbIntervals];
		proposedDepartTime = new double[nbIntervals + 1];

		line = sc.nextLine().trim().split("\\s+");

		for (int i = 0; i < nbIntervals; ++i) {
			proposedDepartTime[i + 1] = Double.parseDouble(line[i]) * depotTimeWindowUpperBound;
		}

		for (int j = 0; j < nbTypes; ++j) {
			line = sc.nextLine().trim().split("\\s+");

		    for (int i = 0; i < nbIntervals; ++i) {
				speedFactors[j][i] = Double.parseDouble(line[i]);
			}
		}

		double[][] fct;
		speedFunctionList = new ArrayList<>();
		arcArrivalTimeFunctions = new ArrivalTimeFunction[nbNodes][nbNodes];
		while(sc.hasNextLine()) {
			line = sc.nextLine().trim().split("\\s+");
		    
		    int from =Integer.parseInt(line[0]);
		    int to =Integer.parseInt(line[1]);
		    int typ = Integer.parseInt(line[2]);

		    fct = new double[nbIntervals][2];

		    for (int i = 0; i < nbIntervals; ++i) {
			  	fct[i][0] = proposedDepartTime[i + 1];
			  	fct[i][1] = (distanceMatrix[from][to] / travelTimes[from][to]) * speedFactors[typ][i];
		  	}

			arcArrivalTimeFunctions[from][to] =
					new PiecewiseArrivalTimeFunction(proposedDepartTime, travelTimes[from][to], speedFactors[typ]);
//			for (double departureTime = 0.0; departureTime <= proposedDepartTime[proposedDepartTime.length - 1] * 3;
//				 departureTime += 10.0) {
//				double arrivalTime1 = DominantShortestPath.getNeighborArrivalTime(distanceMatrix[from][to], departureTime, fct);
//				double arrivalTime2 = arrivalTimeFunctions[from][to].getArrivalTime(departureTime);
//				assert Math.abs(arrivalTime1 - arrivalTime2) < 0.0001;
//			}

			int functionIndex = speedFunctionList.indexOf(fct);
		    if (functionIndex == -1) {
				speedFunctionList.add(fct);
				functionIndex = speedFunctionList.size() - 1;
			}

		    // We add 1 because 0 is used for nonexistent arcs
			speedFunctionMatrix[from][to] = functionIndex + 1;
		}
		sc.close();
    }
}
