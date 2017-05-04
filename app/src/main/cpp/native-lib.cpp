#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdlib.h>
#include <math.h>

extern "C"
int mask [3][3] = {1 ,2 ,1 ,
                   2 ,3 ,2 ,
                   1 ,2 ,1 };


unsigned char getPixel ( unsigned char * arr , int col , int row , int k,int height,int width ) {
    int sum = 0;
    int denom = 0;
    unsigned char pixel;
    for ( int j = -1; j <=1; j ++) {
        for ( int i = -1; i <=1; i ++) {
            if ((row + j) >= 0 && (row + j) < height && (col + i) >= 0 && (col + i) < width) {
                unsigned char color = arr [( row + j ) * 3 * width + ( col + i ) * 3 + k];
                sum += color * mask [ i +1][ j +1];
                denom += mask [ i +1][ j +1];
            }
        }
    }
    pixel =(unsigned char) ( (sum / denom) > 255)? 255:(sum / denom);
    pixel =(unsigned char) ( (sum / denom) < 0)? 0:(sum / denom);
    return pixel;
}
void h_blur ( unsigned char * arr , unsigned char * result,int height,int width ) {
    for ( int row =0; row < height; row ++) {
        for ( int col =0; col < width; col ++) {
            for (int k = 0; k < 3; k++) {
                if( (3 * row * width + 3 * col + k) <=  height*width){
                    arr [ 3 * row * width + 3 * col + k] = getPixel ( arr , col , row , k, height, width ) ;
                }

            }
        }
    }
}

struct cannyAlgorithm
{
    unsigned char * image;

    unsigned width;
    unsigned height;
    unsigned size;
    unsigned bpp;

    short * Gx;
    short * Gy;

    short * Direction;

    double * Magnitude;

    cannyAlgorithm( unsigned char * data, unsigned imgw, unsigned imgh, unsigned bitsperpixel )
    {
        width   = imgw;
        height  = imgh;

        size = width* height;

        bpp = bitsperpixel;

        Gx = new short[ size ];
        Gy = new short[ size ];

        Direction = new short[ size ];

        Magnitude = new double[ size ];

        image = new unsigned char[ size* bpp ];
        memcpy( image, data, size* bpp );
    }
    ~cannyAlgorithm( void )
    {
        delete[] Gx;
        delete[] Gy;
        delete[] Direction;
        delete[] Magnitude;
    }
    int max( int a, int b )
    {
        int c = a > b ? a : b;
        return c;
    }
    int min( int a, int b )
    {
        int c = a < b ? a : b;
        return c;
    }
    short getAngle( double X, double Y )
    {
        short Angle;

        if( X* Y > 0 )  // Quadrant 1 or 3
        {
            if( abs( X ) >= abs( Y ) )
                Angle = 0;
            else
                Angle = 180;
        }
        else            // Quadrant 2 or 4
        {
            if( abs(X) >= abs(Y) )
                Angle = 90;
            else
                Angle = 270;
        }

        return( Angle );
    }
    double hypotenuse( double a, double b )
    {
        double h = sqrt( a*a + b*b );
        return(h);
    }
    bool isLocalMax( unsigned offset )
    {
        unsigned bottom     = max(offset - width, 0);
        unsigned top        = min(offset + width, size);
        unsigned left       = max(offset - 1, 0);
        unsigned right      = min(offset + 1, size);
        unsigned bottomLeft = max(bottom - 1, 0);
        unsigned bottomRight= min(bottom + 1, size);
        unsigned topLeft    = max(top - 1, 0);
        unsigned topRight   = min(top + 1, size);

        double thisPoint = 0.0;
        double mag[2]    = { 0.0 };

        switch( Direction[offset] )
        {
            case 0:
            {
                /*   90
                      *
                      |******
                      |******
                -------------* 0
                      |
                      |

                */
                thisPoint = abs( Gx[offset]* Magnitude[offset] );

                mag[0] = abs( Gy[offset]* Magnitude[topRight  ] + ( Gx[offset] - Gy[offset] )* Magnitude[right] );
                mag[1] = abs( Gy[offset]* Magnitude[bottomLeft] + ( Gx[offset] - Gy[offset] )* Magnitude[left ] );
            }break;

            case 90:
            {
                /*
                      90
                      *
                 *****|
                 *****|
            180 *-------------
                      |
                      |
                */
                thisPoint = abs(Gx[offset]* Magnitude[offset] );

                mag[0] = abs( Gy[offset]* Magnitude[bottomRight] - ( Gx[offset] + Gy[offset])* Magnitude[right] );
                mag[1] = abs( Gy[offset]* Magnitude[topLeft    ] - ( Gx[offset] + Gy[offset])* Magnitude[left ] );
            }break;

            case 180:
            {
                /*
                      |
                      |
            180 *-------------
                ******|
                ******|
                      *
                     270
                */
                thisPoint = abs( Gy[offset]* Magnitude[offset] );

                mag[0] = abs( Gx[offset]* Magnitude[topRight  ] + ( Gy[offset] - Gx[offset] )* Magnitude[top   ] );
                mag[1] = abs( Gx[offset]* Magnitude[bottomLeft] + ( Gy[offset] - Gx[offset] )* Magnitude[bottom] );
            }break;

            case 270:
            {
                /*
                      |
                      |
                -------------* 0
                      |*******
                      |*******
                      *
                     270
                */
                thisPoint = abs( Gy[offset]* Magnitude[offset] );

                mag[0] = abs( Gx[offset]* Magnitude[bottomRight] - ( Gy[offset] + Gx[offset] )* Magnitude[bottom] );
                mag[1] = abs( Gx[offset]* Magnitude[topLeft    ] - ( Gy[offset] + Gx[offset] )* Magnitude[top   ] );
            }break;

            default:
                break;
        }

        if( thisPoint >= mag[0] && thisPoint >= mag[1] )
            return( true );
        return( false );
    }
    void grayScaleCompress( void )
    {
        unsigned char * compressed = new unsigned char[ size ];

        for( unsigned offset = 0; offset < size; offset++ )
            compressed[offset] = image[offset* bpp];

        delete[] image;
        image = new unsigned char[ size ];
        memcpy( image, compressed, size );

        delete[] compressed;
    }
    void continuousTracing( unsigned offset, unsigned char * in, unsigned char * out, unsigned thresholding )
    {
        /*
        The concept is sample:
        I found a possible edge and I will follow this edge until its end.
        Test 8 neighboring pixels and if someone is higher than thresholding then
        that pixel will be another edge and I will follow it.

        This process is repeated until the value of the current pixel tested is null.
        */
        const unsigned edge = 255;

        unsigned dir[2];
        dir[0] = width;      // Top - Bottom
        dir[1] = 1;          // Left - Right

        unsigned top = min( offset + dir[0], size );
        if( in[top] >= thresholding )
            do
            {
                if( !out[top] )
                {
                    out[top] = edge;
                    continuousTracing( top, in, out, thresholding );
                }
                else
                    break;

                top += dir[0];

                if( top > size )
                    break;

            }while( in[top] >= thresholding );

        unsigned bottom = max( offset - dir[0], 0 );
        if( in[bottom >= thresholding] )
            do
            {
                if( !out[bottom] )
                {
                    out[bottom] = edge;
                    continuousTracing( bottom, in, out, thresholding );
                }
                else
                    break;

                bottom -= dir[0];

                if( bottom < 0 )
                    break;

            }while( in[bottom] >= thresholding );

        unsigned right = min( offset + dir[1], size );
        if( in[right] >= thresholding )
            do
            {
                if( !out[right] )
                {
                    out[right] = edge;
                    continuousTracing( right, in, out, thresholding );
                }
                else
                    break;

                right += dir[1];

                if( right > size )
                    break;

            }while( in[right] >= thresholding );

        unsigned left = max( offset - dir[1], 0 );
        if( in[left] >= thresholding )
            do
            {
                if( !out[left] )
                {
                    out[left] = edge;
                    continuousTracing( left, in, out, thresholding );
                }
                else
                    break;

                left -= dir[1];

                if( left < 0 )
                    break;

            }while( in[left] >= thresholding );

        unsigned topRight = min( offset + dir[0] + dir[1], size );
        if( in[topRight] >= thresholding )
            do
            {
                if( !out[topRight] )
                {
                    out[topRight] = edge;
                    continuousTracing( left, in, out, thresholding );
                }
                else
                    break;

                topRight += dir[0] + dir[1];

                if( topRight > size )
                    break;

            }while( in[topRight] >= thresholding );

        unsigned bottomLeft = max( offset - dir[0] - dir[1], 0 );
        if( in[bottomLeft] >= thresholding )
            do
            {
                if( !out[bottomLeft] )
                {
                    out[bottomLeft] = edge;
                    continuousTracing( bottomLeft, in, out, thresholding );
                }
                else
                    break;

                bottomLeft -= dir[0] - dir[1];

                if( bottomLeft < 0 )
                    break;

            }while( in[bottomLeft] >= thresholding );

        unsigned topLeft = min( offset + dir[0] - dir[1], size );
        if( in[topLeft] >= thresholding )
            do
            {
                if( !out[topLeft] )
                {
                    out[topLeft] = edge;
                    continuousTracing( topLeft, in, out, thresholding );
                }
                else
                    break;

                topLeft += dir[0] - dir[1];

                if( topLeft > size )
                    break;

            }while( in[topLeft] >= thresholding );

        unsigned bottomRight = max( offset - dir[0] + dir[1], 0 );
        if( in[bottomRight] >= thresholding )
            do
            {
                if( !out[bottomRight] )
                {
                    out[bottomRight] = edge;
                    continuousTracing( bottomRight, in, out, thresholding );
                }
                else
                    break;

                bottomRight -= dir[0] + dir[1];

                if( bottomRight < 0 )
                    break;

            }while( in[bottomRight] >= thresholding );

        /* Works with feedback and not will be an infinite loop cause I am saving the new data into a new image */
    }
    void computeGradients( void )
    {
        // Compute Gradients in X
        for( unsigned y = 0; y < height; y++ )
        {
            unsigned offset = y* width;

            Gx[offset] = image[offset + 1] - image[offset];

            offset++;
            for( unsigned x = 1; x < width - 1; x++, offset++ )
                Gx[offset] = image[offset + 1] - image[offset - 1];

            Gx[offset] = image[offset] - image[offset - 1];
        }
        // Compute Gradients in Y
        for( unsigned x = 0; x < width; x++ )
        {
            unsigned offset = x;

            Gy[offset] = image[offset + width] - image[offset];

            offset += width;
            for( unsigned y = 1; y < height - 1; y++, offset += width )
                Gy[offset] = image[offset + width] - image[offset - width];

            Gy[offset] = image[offset] - image[offset - width];
        }
        // Hypotenuse = sqrt(x^2 + y^2)
        for( unsigned y = 0, offset = 0; y < height; y++ )
            for( unsigned x = 0; x < width; x++, offset++ )
                Magnitude[offset] = hypotenuse( Gx[offset], Gy[offset] );
        // Okay, edges of the image must be null
        for( unsigned x = 0; x < width; x++ )
            Magnitude[x] = 0;

        for( unsigned x = 0, offset = width* (height - 1); x < width; x++, offset++ )
            Magnitude[offset] = 0;

        for( unsigned y = 0; y < width* height; y += width )
            Magnitude[y] = 0;

        for( unsigned y = 0, offset = width - 1; y < width* height; y += width, offset += width)
            Magnitude[offset] = 0;
    }
    void nonMaxSupress( void )
    {
        /* Compute magnitudes direction and save it */
        for( unsigned y = 0, offset = 0; y < height; y++ )
            for( unsigned x = 0; x < width; x++, offset++ )
                Direction[offset] = getAngle( Gx[offset], Gy[offset] );
        /*
            The most complicated part:
            If the pixel is not a local maximum then kill it.
            How do I know if my point is a local max ?
            I will compare the current pixel with neighboring pixels in the gradient direction.
            Remember: Pixel with null magnitude are not candidate to be an edge.
        */
        for( unsigned y = 0, offset = 0; y < height; y++ )
            for( unsigned x = 0; x < width; x++, offset++ )
            {
                if( Magnitude[offset] && isLocalMax(offset) )
                    image[offset] = Magnitude[offset] > 255 ? 255 : (unsigned char)Magnitude[offset];
                else
                    image[offset] = 0;
            }
    }
    void hysteresis( float lowScale, float highScale )
    {
        /*
        We need a High value and a Low value, High value will be the edge color.
        All pixels with color higher than ' Hight value ' will be edges
        and we will follow this pixel until another pixel is founded.
        The pixel founded must be a color higher than ' Low value ', if that is the case
        then we will set this pixel like edge, else it will be background ( null ).
        */

        lowScale    = lowScale <= 0.0f ? 0.01f : lowScale > 1.0f ? 1.0f : lowScale;
        highScale   = highScale <= 0.0f ? 0.01f : highScale > 1.0f ? 1.0f : highScale;

        unsigned char globalMax = 0;
        for( unsigned offset = 0; offset < size; offset++ )
            if( image[offset] > globalMax )
                globalMax = image[offset];

        unsigned highV = globalMax* highScale;
        unsigned lowV = globalMax* lowScale;

        unsigned char * finalPic = new unsigned char[ size ];
        memset( finalPic, 0, size );

        for( unsigned y = 1,offset = 1; y < height - 1; y++ )
            for( unsigned x = 1; x < width - 1; x++, offset++ )
                if( image[offset] >= highV && !finalPic[offset] )
                {
                    finalPic[offset] = 255;
                    continuousTracing( offset, image, finalPic, lowV );
                }

        delete[] image;
        image = new unsigned char[ size ];
        memcpy( image, finalPic, size );

        delete[] finalPic;
    }
    void grayScaleDecompress( void )
    {
        size = width* height* bpp;
        unsigned char * decompressed = new unsigned char[ size ];

        for( unsigned offset = 0; offset < width* height; offset++ )
            decompressed[offset*bpp + 0] = decompressed[offset* bpp + 1] = decompressed[offset* bpp + 2] = image[offset];

        delete[] image;
        image = new unsigned char[ size ];
        memcpy( image, decompressed, size );

        delete[] decompressed;
    }
    void AutoEdgeDetection( unsigned char * data, float lowV, float highV )
    {
        grayScaleCompress();
        computeGradients();
        nonMaxSupress();
        hysteresis(lowV, highV);
        grayScaleDecompress();

        memcpy( data, image, size );
    }
    unsigned char * get_data( void )
    {
        grayScaleDecompress();
        return( image );
    }
};



void imprimirEntero(char letra,int entero){
    char str_i[80];
    sprintf(str_i,  " imprime %c Entero: %d",letra,entero);
    __android_log_write(ANDROID_LOG_ERROR, "frame: ", str_i);//Or ANDROID_LOG_INFO, ...
}
void imprimeNumero(int entero){
    char str_i[80];
    sprintf(str_i,  "%x",entero);
    __android_log_write(ANDROID_LOG_ERROR, "frame: ", str_i);//Or ANDROID_LOG_INFO, ...
}


int convertYUVtoRGB(int y, int u, int v) {
    int r,g,b;

    r = y + (int)1.402f*v;
    g = y - (int)(0.344f*u +0.714f*v);
    b = y + (int)1.772f*u;

//    r = y + (int)1.370705f*v;
//    g = y - (int)(0.698001f*v -0.337633f*u);
//    b = y + (int)1.732446f*u;

    r = r>255? 255 : r<0 ? 0 : r;
    g = g>255? 255 : g<0 ? 0 : g;
    b = b>255? 255 : b<0 ? 0 : b;
    return 0xff000000 | (b<<16) | (g<<8) | r;
}

void convertYUV420_NV21toRGB8888(unsigned char *data,int *poutPixels,int width,int height) {
    int size = width*height;    //int size=((height *3/4) * (width*3/4))+((height *3/4) * (width *3/4))/2;
    //int size=((height *3/4) * (width*3/4))+((height *3/4) * (width *3/4))/2;
//    imprimirEntero('s',size);
    //  int offset = (((height *3)/4) * ((width*3)/4));
    int offset=size;
    int u, v, y1, y2, y3, y4;
    int i,k;

    // i percorre os Y and the final pixels
    // k percorre os pixles U e V
    for(i=0, k=0; i < size; i+=2, k+=2) {
        y1 = data[i  ]&0xff;
        y2 = data[i+1]&0xff;
        y3 = data[(width)+i  ]&0xff;
        y4 = data[(width)+i+1]&0xff;

        u = data[offset+k  ]&0xff;
        v = data[offset+k+1]&0xff;
        u = u-128;
        v = v-128;

        poutPixels[i  ] = convertYUVtoRGB(y1, u, v);
        poutPixels[i+1] = convertYUVtoRGB(y2, u, v);
        poutPixels[(width)+i  ] = convertYUVtoRGB(y3, u, v);
        poutPixels[(width)+i+1] = convertYUVtoRGB(y4, u, v);



        if (i!=0 && (i+2)%(width)==0)
            i+=(width);

    }



}

void applyGrayScale(unsigned char *data,int *pixels,int width,int height) {
    int p;
    int size = width*height;
    for(int i = 0; i < size; i++) {
        p = data[i] & 0xFF;
        pixels[i] = 0xff000000 | p<<16 | p<<8 | p;
    }
}

void siSaleDeUnsignedChar(int * pixels,int width,int height) {
    int size = width*height;
    int i;
    int mayor = 0;
    int menor = 0;

    for(i=0; i < size; i++) {
        imprimeNumero(pixels[i]);
        if ( pixels[i] > 255 ) mayor++;
        if ( pixels[i] < 0 ) menor++;
    }
//    imprimirEntero('z',mayor);

}
extern "C"
jboolean
Java_com_example_jorge_testmatrixjni_CameraPreview_stringFromJNI(
        JNIEnv* env, jobject thiz,
        jint width, jint height,
        jbyteArray NV21FrameData, jintArray outPixels) {

    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

    unsigned char * salidaAux;
    salidaAux = ( unsigned char * )malloc( (height * width) * sizeof( unsigned char ) );

//    __android_log_write(ANDROID_LOG_ERROR, "Llega", "630");//Or ANDROID_LOG_INFO, ...
    h_blur ((unsigned char *)pNV21FrameData,(unsigned char *)pNV21FrameData, height, width );
//    __android_log_write(ANDROID_LOG_ERROR, "Llega", "632");//Or ANDROID_LOG_INFO, ...
    cannyAlgorithm cpix((unsigned char *)pNV21FrameData,(unsigned)width,(unsigned)height,(unsigned) 1 );
//    __android_log_write(ANDROID_LOG_ERROR, "Llega", "634");//Or ANDROID_LOG_INFO, ...
    cpix.AutoEdgeDetection((unsigned char *)pNV21FrameData, 0.1f, 0.20f ); // 0.1f, 0.1f
//    __android_log_write(ANDROID_LOG_ERROR, "Llega", "636");//Or ANDROID_LOG_INFO, ...
    applyGrayScale((unsigned char *)pNV21FrameData,(int *) poutPixels,width,height);
//    __android_log_write(ANDROID_LOG_ERROR, "Llega", "638");//Or ANDROID_LOG_INFO, ...
//    cannyAlgorithm cpix((unsigned char *) pNV21FrameData,(unsigned)width,(unsigned)height,(unsigned) 1 );
//    cpix.AutoEdgeDetection((unsigned char *) pNV21FrameData, 0.2f, 0.25f );
//    applyGrayScale((unsigned char *) pNV21FrameData,(int *) poutPixels,width,height);
  //  convertYUV420_NV21toRGB8888((unsigned char *) pNV21FrameData,(int *) poutPixels,width,height);

    return true;
}
