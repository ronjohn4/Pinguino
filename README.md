# Pinguino
Adds you as a selfie to photos you share

There are 2 use cases with Pinguino.

## Define your selfie photo
Pinguino needs a photo of you to add to future shared photos with a transparent background.  Pinguino allows you to select a photo of yourself in front of a solid color (green screen works best).  You can then tap on the background to see it become transparent.  Multiple taps are allowed as no background is perfectly uniform.  Once you have created your sharable selfie photo it is saved as part of the configuration.

Now whenever you share a photo, you can select Pinguino rather than instant messaging.  Pinguino does exactly the same thing as instant messaging but inserts your photo on top of the shared photo, making it look like a selfie.

Technically interesting code is the green screen manipulation.  Removing the tapped color is for contiguous sections so the color isn't removed from the subject of the photo.  It is done raster fashion pixel at a time but in memory during the operation so is very quick.
