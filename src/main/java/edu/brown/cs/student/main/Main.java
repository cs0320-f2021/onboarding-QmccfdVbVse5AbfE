package edu.brown.cs.student.main;

import java.io.*;
import java.net.Proxy;
import java.util.*;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

import javax.swing.plaf.synth.SynthTextAreaUI;

import static java.lang.Integer.parseInt;

/**
 * The comparitor of the project. Helps with custom sorting
 */

class StarComparator implements Comparator<Star> {

  public int compare(Star o1, Star o2) {
    if (o1.distance < o2.distance){
      return -1;
    } else if (o1.distance > o2.distance){
      return 1;
    }
    else {
      return 0;
    }
  }
}


/**
 * The Main class of our project. This is where execution begins.
 */

public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    // TODO: Add your REPL here!
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;
      HashMap<String, Star > star_dict = new HashMap<String, Star>();
      while ((input = br.readLine()) != null) {
        try {
          input = input.trim();
          String[] arguments = input.split(" ");
          System.out.println(arguments[0]);
          Double output;
          if (arguments[0].toLowerCase().equals("add")){
            output = MathBot.add(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]));
            System.out.println(output);
          } else if (arguments[0].toLowerCase().equals("subtract")){
            output = MathBot.subtract(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]));
            System.out.println(output);
          } else if (arguments[0].toLowerCase().equals("stars")) {
            star_dict = new HashMap<String, Star>();
            String file = arguments[1];
            // the following code was based off of https://www.baeldung.com/java-csv-file-array
            try (BufferedReader fr = new BufferedReader(new FileReader(file))) {
              String currLine;
              int num_times = 0;
              while ((currLine = fr.readLine()) != null) {
                String[] values = currLine.split(",");
                if (num_times == 0){
                  //check to see csv has proper headings (if improper enter if statement
                  //StarID,ProperName,X,Y,Z
                  if (values[0].equals("StarID") &! values[1].equals("ProperName") &! values[2].equals("X")
                          &! values[3].equals("Y") &! values[4].equals("Z")) {
                    System.out.println("ERROR: File has improper headings");
                  }
                } else {
                  star_dict.put(values[1], new Star(values[0],values[1],values[2],values[3], values[4]));
                }
                num_times ++;
              }
            } catch (FileNotFoundException e) {
              System.out.println("ERROR: We couldn't find your file");
            } catch (IOException e) {
              System.out.println("ERROR: We couldn't process your input 1");
              e.printStackTrace();
            }
          } else if (arguments[0].toLowerCase().equals("where")){
            System.out.println(star_dict);
          } else if (arguments[0].toLowerCase().equals("naive_neighbors")){
            //            needs to output a list of k nearest stars to given location
            int k_stars = parseInt(arguments[1]);
            String x = "0";
            String y = "0";
            String z = "0";
            Star star_finding;
            if(arguments.length == 2) {
              star_finding = star_dict.get(arguments[2]);
            } else if (arguments.length == 5) {
              star_finding = new Star("-1", "Searching_star", arguments[2], arguments[3],arguments[4]);
            } else {
              System.out.println("ERROR: We couldn't find your star. Check your query.");
              break;
            }
            Collection<Star> list_of_stars = star_dict.values();
            for (Star i : list_of_stars) {
              System.out.println(i.id);
              Star.distance = MathBot.distance(Double.parseDouble(star_finding.x),Double.parseDouble(star_finding.y),
                      Double.parseDouble(star_finding.z), Double.parseDouble(i.x), Double.parseDouble(star_finding.y), Double.parseDouble(star_finding.z));
            }
            ArrayList<Star> real_star_list = new ArrayList<>(list_of_stars);
            real_star_list.sort(new StarComparator());
          }

        } catch (Exception e) {
           e.printStackTrace();
          System.out.println("ERROR: We couldn't process your input");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: Invalid input for REPL");
    }

  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_0);

    // this is the directory where FreeMarker templates are placed
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    // set port to run the server on
    Spark.port(port);

    // specify location of static resources (HTML, CSS, JS, images, etc.)
    Spark.externalStaticFileLocation("src/main/resources/static");

    // when there's a server error, use ExceptionPrinter to display error on GUI
    Spark.exception(Exception.class, new ExceptionPrinter());

    // initialize FreeMarker template engine (converts .ftl templates to HTML)
    FreeMarkerEngine freeMarker = createEngine();

    // setup Spark Routes
    Spark.get("/", new MainHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      // status 500 generally means there was an internal server error
      res.status(500);

      // write stack trace to GUI
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to serve the site's main page.
   *
   * @return ModelAndView to render.
   * (main.ftl).
   */
  private static class MainHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      // this is a map of variables that are used in the FreeMarker template
      Map<String, Object> variables = ImmutableMap.of("title",
          "Go go GUI");

      return new ModelAndView(variables, "main.ftl");
    }
  }
}
