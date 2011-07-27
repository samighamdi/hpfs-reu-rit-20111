/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeansdemondp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author reu
 */
public class TestClass {
    public static void main(String args[]) throws ParseException {
        try {
            double[][] array = new double[20][5];
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 5; j++) {
                    array[i][j] = j + i;
                }
            }
            ArrayDoubleFS fs = new ArrayDoubleFS("test", array);
            System.out.println(fs.size());
            System.out.println(fs.innerSize());
            fs.close();
            fs = new ArrayDoubleFS(new File("kmeansdemopoints.adfs"));
            System.out.println(fs.size());
            System.out.println(fs.innerSize());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
