var JavaPackages = new JavaImporter(
    Packages.org.joml.Vector3f,
    Packages.tage.Light
);

with(JavaPackages){
    var cameraLocation = new Vector3f(0.0, 64.0, 0.0);
    var light = new Light();
    light.setLocation(new Vector3f(25.0, 50.0, 10.0));
    var lightGlobalAmbient = new Vector3f(0.5, 0.5, 0.5);
    var playerSpeed = 24.0;
}