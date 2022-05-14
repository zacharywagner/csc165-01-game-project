var JavaPackages = new JavaImporter(
    Packages.org.joml.Vector3f,
    Packages.tage.Light
);

with(JavaPackages){
    var cameraLocation = new Vector3f(0.0, 64.0, 0.0);
    var light = new Light();
    light.setLocation(new Vector3f(25.0, 50.0, 10.0));
    light.setAmbient(0.2, 0.2, 0.2);
    var lightGlobalAmbient = new Vector3f(0.5, 0.5, 0.5);
    var playerSpeed = 24.0;
    var playerStartLocation = new Vector3f(0.0, 0.0, 24.0);
    var terrainHeight = -12.0;
    var terrainScale = new Vector3f(48.0, 24.0, 48.0);
    var bossAmplitude = 20.0;
    var bossPeriod = 2.0;
    var bossHealth = 500;
    var playerHealth = 100;
}