package images;

import javafx.scene.image.Image;

/**
 * Created by Radoslav Ralinov on 29/02/2016. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class ApplicationImages {
    public Image SCAN_IMG;
    public Image STOP_BUTTON_IMG;
    public Image PAUSE_IMG;

    public ApplicationImages()
    {
        SCAN_IMG =  new Image(getClass().getResourceAsStream("/images/magnifier.png"));
        STOP_BUTTON_IMG = new Image(getClass().getResourceAsStream("/images/stop.png"));
        PAUSE_IMG = new Image(getClass().getResourceAsStream("/images/pause.png"));
    }

    public Image getSCAN_IMG() {
        return SCAN_IMG;
    }

    public Image getPAUSE_IMG() {
        return PAUSE_IMG;
    }

    public Image getSTOP_BUTTON_IMG() {
        return STOP_BUTTON_IMG;
    }
}
