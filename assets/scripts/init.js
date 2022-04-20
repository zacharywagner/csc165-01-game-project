// initial orbit controller values
var startingAzimuth = 0.0;
var startingElevation = 85.0; 
var startingRadius = 30.0;

// initial player values
var playerHealth = 3;
var playerSpeed = 0.01;

// hud color
var red = 0.0;
var green = 0.75;
var blue = 0.25;

var JavaPackages = new JavaImporter(
    Packages.org.joml.Vector3f,
    Packages.tage.Light
);

with(JavaPackages){
    var terrainLocalScale = new Vector3f(80.0, 12.0, 80.0);
    var terrainLocalLocation = new Vector3f(0.0, -8.0, 0.0);
    var light = new Light();
    light.setLocation(new Vector3f(5.0, 10.0, 2.0));
}