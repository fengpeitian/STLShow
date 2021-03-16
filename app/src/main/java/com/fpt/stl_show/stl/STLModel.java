package com.fpt.stl_show.stl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/12 10:35
 *   desc    :
 * </pre>
 */
public class STLModel implements Parcelable {

    public float[] color = new float[] {0.75f, 0.75f, 0.75f, 1.0f};

    public float xTranslate = 0;
    public float yTranslate = -2.0f;
    public float zTranslate = -25.0f;
    public float xRotateAngle = 0;
    public float yRotateAngle = 0;
    public float zRotateAngle = 0;
    public float xScale = 1.0f;
    public float yScale = 1.0f;
    public float zScale = 1.0f;

    public float adjustScale = 1.0f;

    public float maxX;

    public float minX;

    public float maxY;

    public float minY;

    public float maxZ;

    public float minZ;

    private float[] vertex_array;

    private float[] normal_array;

    public STLModel() {}

    public void setMax(float maxX, float maxY, float maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void setMin(float minX, float minY, float minZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
    }

    public void setVertexArray(float[] array) {
        this.vertex_array = array;
    }

    public void setNormalArray(float[] array) {
        this.normal_array = array;
    }

    public float[] getVertexArray() {
        return vertex_array;
    }

    public float[] getNormalArray() {
        return normal_array;
    }

    public boolean isDataEmpty() {
        return normal_array == null || vertex_array == null;
    }

    /**
     *  初始化模型缩放倍数，使其完全显示在屏幕上
     */
    public void adjustScale() {
        float maxSize = maxX - minX;

        if (maxSize < maxY - minY){
            maxSize = maxY - minY;
        }
        if (maxSize < maxZ - minZ){
            maxSize = maxZ - minZ;
        }

        if (maxSize > 20.0f) {
            adjustScale = 18.0f/maxSize;
        } else if(maxSize < 10.0f) {
            adjustScale = 15.0f/maxSize;
        }
    }

    //-----------------------------------Parcelable-----------------------------------

    protected STLModel(Parcel in) {
        color = in.createFloatArray();
        xTranslate = in.readFloat();
        yTranslate = in.readFloat();
        zTranslate = in.readFloat();
        xRotateAngle = in.readFloat();
        yRotateAngle = in.readFloat();
        zRotateAngle = in.readFloat();
        xScale = in.readFloat();
        yScale = in.readFloat();
        zScale = in.readFloat();
        maxX = in.readFloat();
        minX = in.readFloat();
        maxY = in.readFloat();
        minY = in.readFloat();
        maxZ = in.readFloat();
        minZ = in.readFloat();
        vertex_array = in.createFloatArray();
        normal_array = in.createFloatArray();
    }

    public static final Creator<STLModel> CREATOR = new Creator<STLModel>() {
        @Override
        public STLModel createFromParcel(Parcel in) {
            return new STLModel(in);
        }

        @Override
        public STLModel[] newArray(int size) {
            return new STLModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloatArray(color);
        parcel.writeFloat(xTranslate);
        parcel.writeFloat(yTranslate);
        parcel.writeFloat(zTranslate);
        parcel.writeFloat(xRotateAngle);
        parcel.writeFloat(yRotateAngle);
        parcel.writeFloat(zRotateAngle);
        parcel.writeFloat(xScale);
        parcel.writeFloat(yScale);
        parcel.writeFloat(zScale);
        parcel.writeFloat(maxX);
        parcel.writeFloat(minX);
        parcel.writeFloat(maxY);
        parcel.writeFloat(minY);
        parcel.writeFloat(maxZ);
        parcel.writeFloat(minZ);
        parcel.writeFloatArray(vertex_array);
        parcel.writeFloatArray(normal_array);
    }

}
