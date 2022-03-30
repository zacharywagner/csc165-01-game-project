package tage.shapes;

import tage.*;

/**
* Abstract class for user-defined manual objects.
* <p>
* A manual object is one in which the user has manually specified the
* vertex locations, texture coordinates, and normal vectors for the object.
* When defining a custom manual object, the class must extend ManualObject, and
* provide a constructor that does the following steps, in this order:
* <OL>
* <LI> call super()
* <LI> set the number of vertices using setVertices()
* <LI> generate fields for vertices, texture coordinates, and normal vectors (either float arrays or arrays of Vector3f)
* <LI> load them into the Shape arrays using the appropriate setters
* <LI> set the winding order if other than CCW, and primitive type if other than 3 (triangles)
* </OL>
* @author Scott Gordon
*/
public abstract class ManualObject extends ObjShape
{
	public ManualObject()
	{	super();
	}
}