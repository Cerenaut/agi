/*
 * Copyright (c) 2017.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.entities.stdp;

import io.agi.core.data.*;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 6/05/17.
 */
public class DifferenceOfGaussiansEntity extends Entity {

    public static final String ENTITY_TYPE = "difference-of-gaussians";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_KERNEL = "kernel";
    public static final String DATA_OUTPUT = "output";

    public DifferenceOfGaussiansEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_KERNEL );
        attributes.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return DifferenceOfGaussiansEntityConfig.class;
    }

    public void doUpdateSelf() {

        // https://grey.colorado.edu/mediawiki/sites/CompCogNeuro/images/thumb/5/56/fig_on_off_rfields_edge.png/400px-fig_on_off_rfields_edge.png
        DifferenceOfGaussiansEntityConfig config = ( DifferenceOfGaussiansEntityConfig ) _config;

        Data input = getData( DATA_INPUT );

        Point inputSize = Data2d.getSize( input );

        int w = inputSize.x;
        int h = inputSize.y;

        Data kernel = getData( DATA_KERNEL );
        Data output = new Data( DataSize.create( w, h ) );

        // check for kernel param changes
        String kernelDesc = getKernelDesc( config );
        if( !kernelDesc.equals( config.kernelDescCached ) ) {
            config.kernelDescCached = kernelDesc;
            kernel = getKernel( config );
            setData( DATA_KERNEL, kernel );
        }

        Convolution2d.convolve( kernel, input, output );

        output.mul( config.outputFactor );
        output.clipRange( config.clipMin, config.clipMax );
        output.scaleRange( config.scaleMin, config.scaleMax );

        setData( DATA_OUTPUT, output );
    }

    /**
     * Kernel calculation confirmed from DoG.m in paper code.
     * @param config
     * @return
     */
    protected Data getKernel( DifferenceOfGaussiansEntityConfig config ) {
        Data kernel = Kernels2d.DifferenceOfGaussians( config.kernelWidth, config.kernelHeight, config.stdDev1, config.stdDev2 );
        float mean = (float)kernel.mean();
        kernel.sub( mean ); // now mean is zero
        float max = kernel.max();
        float min = Math.abs( kernel.min() );
        float absMax = Math.max( max, min );
        kernel.div( absMax ); // max is now 1
        return kernel;
    }

    protected String getKernelDesc( DifferenceOfGaussiansEntityConfig config ) {
        String s = config.kernelWidth + "," + config.kernelHeight + "," + config.stdDev1 + "," + config.stdDev2;// + "," + config.scaling;
        return s;
    }
}

/*

Steps in Matlab code:
1. Image resize
2. DoG filter
3. image padding
4. On/Off thresholding
5. Local norm of the DoG output (*radius* 8)
6. Spike ordering (all pixels spike, it's a matter of when?)

b = mod( a , m ) returns the remainder after division of a by m , where a is the dividend and m is the divisor. This function is often called the modulo operation, which can be expressed as b = a - m.*floor(a./m) . The mod function follows the convention that mod(a,0) returns a .

    %========Converting DoG activites into latencies of input spike=========
    [latencies, I]=sort(1./img(:));%conversion and sorting based on times
    I(isinf(latencies))=[]; %removing spikes with infinite latency
    row=mod((mod(I-1,size(img,1)*size(img,2))+1)-1,size(img,1))+1;%computing the y-coordinate of each spike
    col=ceil((mod(I-1,size(img,1)*size(img,2))+1)/size(img,1))-1;%computing the x-coordinate of each spike
    pag=ceil(I/(size(img,1)*size(img,2)))-1;%computing the z-coordinate of each spike (the ON or OFF layer)

    % Note that spikes are propagated in descrite time steps with equal number of spikes, thus each spike has a time step
    TimeStep=ceil( (1:length(I))
                   / ( length(I) / ( PresentationTime-COMMON.NumOfLayers ) ) ) -1;

    SpikeList{1}=gpuArray.zeros(size(img,1),size(img,2),size(img,3),PresentationTime);% SpikeList{lay} contains the spikes of layer lay in all positions and time steps
    SpikeList{1}((row+col*size(img,1)+pag*size(img,1)*size(img,2))+(TimeStep'*size(img,1)*size(img,2)*size(img,3)))=1;% filling the spikeList of input layer


https://askubuntu.com/questions/645600/how-to-install-octave-4-0-0-in-ubuntu-14-04

sudo apt-get install liboctave-dev

pkg install -forge image
pkg load image


img = imread( '../cycle10/postproc_aaaaa_5_00.png' );
DoGfilter=DoG(7,1,2);
img2=imfilter(img,DoGfilter);
imshow( img2 );


dave@dave-W35xSTQ-370ST ~/Desktop/agi/stdp deep cnn/SDNN_STDP/Codes $ grep -r 'DoG' ./*
./classification_SDNN.m:            img=imfilter(img,DoGfilter);
./DoG.m:function filter = DoG(sz,sigma1,sigma2)
./globals.m:global DoGfilter
./params.m:%DoG settings
./params.m:DoGfilter=DoG(7,1,2);
./SDNN_GUI.m:    %=============== applying DoG over input image ============================
./SDNN_GUI.m:    img=imfilter(img,DoGfilter);
./SDNN_GUI.m:    imgON(imgON<15)=0;% thresholding the DoG output
./SDNN_GUI.m:    img(:,:,1)=0;% you can add OFF DoG activites here
./SDNN_GUI.m:    %==========local normalization of the DoG output========================
./SDNN_GUI.m:    %========Converting DoG activites into latencies of input spike=========

dave@dave-W35xSTQ-370ST ~/Desktop/agi/stdp deep cnn/SDNN_STDP/Codes $ grep -r 'NormalizatioWindowSize' ./*
    COMMON.Layer{1}.outputSize=size(img);
./classification_SDNN.m:            img=feval(dogLocalNormalizationKernel,img,img,[COMMON.Layer{1}.outputSize,3],NormalizatioWindowSize);
./globals.m:global NormalizatioWindowSize
./params.m:NormalizatioWindowSize=8;
./SDNN_GUI.m:    img=feval(dogLocalNormalizationKernel,img,img,[COMMON.Layer{1}.outputSize,3],NormalizatioWindowSize);

octave:4> DoGfilter
        DoGfilter =

        0.063963  -0.011208  -0.078225  -0.103131  -0.078225  -0.011208   0.063963
        -0.011208  -0.127535  -0.161281  -0.131441  -0.161281  -0.127535  -0.011208
        -0.078225  -0.161281   0.116569   0.433004   0.116569  -0.161281  -0.078225
        -0.103131  -0.131441   0.433004   1.000000   0.433004  -0.131441  -0.103131
        -0.078225  -0.161281   0.116569   0.433004   0.116569  -0.161281  -0.078225
        -0.011208  -0.127535  -0.161281  -0.131441  -0.161281  -0.127535  -0.011208
        0.063963  -0.011208  -0.078225  -0.103131  -0.078225  -0.011208   0.063963
*/