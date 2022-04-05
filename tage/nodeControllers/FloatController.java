package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;

/**
* A FloatController is a node controller that, when enabled, will cause an
* object to float up and down relative to its height when the controller is
* instantiated.
* @author Nicholas Appert
*/
public class FloatController extends NodeController {
   private float defaultHeight, newHeight;
   private Vector3f oldPos, newPos;
   private double totalTime = 0;
   private double cycleTime = 360;
   private double amplitude;
   private double period;

   /**
   * Creates a float controller with period and amplitude 1.
   *
   *
   * @param e The engine which the node controller will be handled by
   * @param dh The central height the object will float around
   */ 
   public FloatController(Engine e, float dh) {
      super();
      defaultHeight = dh;
      amplitude = 1;
      period = 1;
   }

   /**
   * Creates a float controller with specified parameters.
   *
   *
   * @param e The engine which the node controller will be handled by
   * @param dh The central height the object will float around
   * @param a The amount the object will deviate from the default height
   * @param p the speed the object will move while floating.
   */
   public FloatController(Engine e, float dh, double a, double p) {
      super();
      defaultHeight = dh;
      amplitude = a;
      period = p;
   }
   
   /** Sets the default height which the object floats with respect to. */
   public void setDefaultHeight(float dh) {
      defaultHeight = dh;
      totalTime = 0;
   }
   
   /** Sets the maximum height deviation from the defaultHeight. */
   public void setAmplitude(double a) {
      amplitude = a;
   }

   /** Sets the speed at which the object will move while floating. */
   public void setPeriod(double p) {
      period = p;
   }

   /** This is called automatically by the RenderSystem (via SceneGraph) once per frame
   *   during display().  It is for engine use and should not be called by the application.
   */   
   public void apply(GameObject go) {
      float elapsedTime = super.getElapsedTime();
      totalTime += elapsedTime/1000.0f;
      
      if (totalTime >= cycleTime) {
         totalTime = 0;
      }
      
      oldPos = go.getLocalLocation();
      newHeight = (float)(amplitude * Math.sin(period * totalTime));
      newPos = new Vector3f(oldPos.x(), defaultHeight + newHeight, oldPos.z());
      go.setLocalLocation(newPos);
   }
}