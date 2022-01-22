package engine;

import engine.time.Delta;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private String title;
    private int width;
    private int height;
    private float scale;
    private final long windowHandle;
    private boolean resized;
    private boolean vSync;
    private int dumpedKey = -1;
    private final Vector3f currentClearColor = new Vector3f();
    private final Vector3f clearColorGoal  = new Vector3f();
    private boolean fullScreen = false;
    private final AtomicBoolean shouldClose;

    public Window(String title, boolean vSync){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();

        this.title   = title;
        this.width   = d.width/2;
        this.height  = d.height/2;
        this.vSync   = vSync;
        this.resized = false;

        // set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW!");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); //the window will be resizable

        //openGL version 4.4
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); //allow auto driver optimizations

        // create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL){
            throw new RuntimeException("Failed to create the GLFW window!");
        }

        // setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (thisWindow, thisWidth, thisHeight) -> {
            width = thisWidth;
            height = thisHeight;
            setWindowResized(true);
        });

        // set up a key callback. it will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            //data stream of key inputs
            if (action == GLFW_PRESS){
                dumpedKey = key;
            } else {
                dumpedKey = -1;
            }
        });

        // make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        if (isvSync()){
            //enable v-sync
            glfwSwapInterval(1);
        }else {
            glfwSwapInterval(0);
        }

        //center window
        glfwSetWindowPos(windowHandle, (d.width - width) / 2, (d.height - height) / 2);

        //make the window visible
        glfwShowWindow(windowHandle);

        GL.createCapabilities();

        //set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //set depth testing
        glEnable(GL_DEPTH_TEST);
        //glDepthFunc(GL_ALWAYS);

        //enable back face culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //hide cursor
        glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        //load icon todo: needs to be it's own class
        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer w = stack.mallocInt(1);
        IntBuffer h = stack.mallocInt(1);
        IntBuffer channels = stack.mallocInt(1);
        ByteBuffer buf = stbi_load("textures/icon.png", w, h, channels, 4);
        GLFWImage image = GLFWImage.malloc();

        //stop crash
        assert buf != null;

        image.set(32,32, buf);
        GLFWImage.Buffer images = GLFWImage.malloc(1);
        images.put(0, image);


        //set icon
        glfwSetWindowIcon(windowHandle, images);

        //free memory
        stbi_image_free(buf);

        //set window state
        this.shouldClose = new AtomicBoolean(false);

        this.updateScale();
    }


    public int getDumpedKey(){
        return dumpedKey;
    }

    public long getWindowHandle(){
        return windowHandle;
    }



    public void setWindowClearColor(float r, float g, float b, float alpha){
        glClearColor(r, g, b, alpha);
        currentClearColor.set(r,g,b);
        clearColorGoal.set(r,g,b);
    }

    public void setWindowClearColorGoal(float r, float g, float b, float alpha){
        clearColorGoal.set(r,g,b);
    }

    public void processClearColorInterpolation(Delta delta){
        currentClearColor.lerp(clearColorGoal, (float) delta.getDelta() * 2f);
        glClearColor(currentClearColor.x, currentClearColor.y, currentClearColor.z,1f);
    }

    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public boolean shouldClose(){
        return this.shouldClose.get();
    }

    public String getTitle(){
        return title;
    }

    public boolean isFullScreen(){
        return fullScreen;
    }

    public void toggleFullScreen(){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        if (!fullScreen) {
            glfwSetWindowMonitor(windowHandle, glfwGetPrimaryMonitor(), d.width / 2, d.height / 2, d.width, d.height, Objects.requireNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor())).refreshRate());
            width = d.width;
            height = d.height;
        }
        else {
            glfwSetWindowMonitor(windowHandle, NULL, d.width / 4, d.height / 4,d.width / 2, d.height / 2, Objects.requireNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor())).refreshRate());
            width = d.width / 2;
            height = d.height / 2;
        }

        setVSync(this.vSync);

        fullScreen = !fullScreen;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public boolean isWindowResized(){
        return resized;
    }

    public void setResized(boolean newResized){
        resized = newResized;
    }

    public boolean getVSync(){
        return vSync;
    }

    public void setVSync(boolean vSync){
        this.vSync = vSync;
        if (vSync){
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }
    }

    public void updateTitle(String newTitle){
        this.title = newTitle;
        glfwSetWindowTitle(windowHandle, newTitle);
    }

    public void windowUpdate(){
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public void updateScale(){
        if (width <= height){
            scale = (float)width;
        } else {
            scale = (float)height;
        }
        System.out.println("Window scale is now: " + scale);
    }

    public float getScale(){
        return this.scale;
    }
}
