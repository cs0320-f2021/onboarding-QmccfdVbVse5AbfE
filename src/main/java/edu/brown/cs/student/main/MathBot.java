package edu.brown.cs.student.main;

import java.lang.Math;


public class MathBot {

  /**
   * Default constructor.
   */
  public MathBot() {

  }

  /**
   * Adds two numbers together.
   *
   * @param num1 the first number.
   * @param num2 the second number.
   * @return the sum of num1 and num2.
   */
  public static double add(double num1, double num2) {
    return num1 + num2;
  }

  /**
   * Subtracts two numbers.
   *
   * @param num1 the first number.
   * @param num2 the second number.
   * @return the difference of num1 and num2.
   */
  public static double subtract(double num1, double num2) {
    return num1 - num2;
  }

  /**
   * Finds the Distance between X_1, Y_1, Z_1 and X_2, Y_2, and Z_2
   *
   * @param X_1 the first star's x value.
   * @param Y_1 the first star's y value.
   * @param Z_1 the first star's z value.
   * @param X_2 the second star's x value.
   * @param Y_2 the second star's y value.
   * @param Z_2 the second star's z value.
   * @return the distance between star 1 and 2.
   */
  public static double distance(Double X_1, Double Y_1, Double Z_1, Double X_2, Double Y_2, Double Z_2) {
    return Math.sqrt(Math.pow((X_2 - X_1), 2) + Math.pow((Y_2 - Y_1), 2) + Math.pow((Z_2 - Z_1), 2));
  }

}


