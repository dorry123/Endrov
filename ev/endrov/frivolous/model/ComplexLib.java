package endrov.frivolous.model;

public class ComplexLib{
	
	static public float[] getRealArray(ComplexArray complex){
		float[] result = new float[complex.length];
		
		for(int i=0; i<complex.length; i++)
			result[i]=complex.real[i];
		
		return result;
	}
	
	static public float[] getRealArrayFirstHalf(ComplexArray complex){
		float[] result = new float[complex.length<<1];
		
		for(int i=0; i<complex.length; i++)
				result[i]=complex.real[i];
		
		return result;
	}

	static public float[] getCombinedArray(ComplexArray complex){
		float[] result = new float[complex.length<<1];
		
		for(int i=0; i<complex.length; i++) {
				result[i<<1]=complex.real[i];
				result[(i<<1)+1]=complex.imag[i];
			}
		
		return result;
	}

	static public float[] getImaginaryArray(ComplexArray complex){
		float[] result = new float[complex.length];
		
		for(int i=0; i<complex.length; i++)
			result[i]=complex.imag[i];
				
		return result;
	}
	
	static public ComplexArray getComplexArray(float[] combined, int w, int h){
		float[] real = new float[combined.length>>1];
		float[] imag = new float[combined.length>>1];
		
		for(int i=0; i<(combined.length>>1); i++){
				real[i] = combined[i<<1];
				imag[i] = combined[(i<<1)+1];
			}
		
		return new ComplexArray(real,imag,w,h);
	}
	
	static public ComplexArray getComplexMultiplication(ComplexArray a, ComplexArray b){
		if(a.length!=b.length)
            throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];
		float[] imag = new float[b.length];
		
		for(int i=0; i<a.length; i++){
			real[i] = a.real[i]*b.real[i]-a.imag[i]*b.imag[i];
			imag[i] = a.real[i]*b.imag[i]+a.imag[i]*b.real[i];
		}
		
		return new ComplexArray(real,imag,a.width,a.height);
	}
	
	static public ComplexArray getRealMultiplication(ComplexArray a, ComplexArray b){
		if(a.length!=b.length)
            throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];
		
		for(int i=0; i<a.length; i++){
			real[i] = a.real[i]*b.real[i];
		}
		
		return new ComplexArray(real,null,a.width,a.height);
	}
	
	static public ComplexArray getRealAddition(ComplexArray a, ComplexArray b){
		if(a.length!=b.length)
            throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];
		
		for(int i=0; i<a.length; i++){
			real[i] = a.real[i]+b.real[i];
		}
		
		return new ComplexArray(real,null,a.width,a.height);
	}
	
	static public ComplexArray getRealSum(ComplexArray[] c){
		float[] real = new float[c[0].length];
		
		for(int i=0; i<c[0].length; i++)
			for(int j=0; j<c.length; j++)
				real[i] += c[j].real[i];
		
		return new ComplexArray(real, null, c[0].width, c[0].height);
	}

	static public ComplexArray getRealDivision(ComplexArray a, ComplexArray b){
		if(a.length!=b.length)
            throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];
		
		for(int i=0; i<a.length; i++){
			real[i] = a.real[i]/b.real[i];

		}
		
		return new ComplexArray(real,null,a.width,a.height);
	}
	
	public static float[] copyArray(float[] array){
		float[] copy = new float[array.length];
		for(int i=0; i<array.length; i++)
			copy[i] = array[i];
		return copy;
	}
	
	public static ComplexArray getFilledArray(ComplexArray sizeOf, float fillValue){
		float[] real = new float[sizeOf.length];
		for(int i = 1; i<real.length; i++)
			real[i] = fillValue;
		return new ComplexArray(real, null, sizeOf.width, sizeOf.height);
	}
	/*
	static public float[] getRealArray(Complex[][] matrix){
		
		return new float[1];
	}
	
	static public float[] getImaginaryArray(Complex[][] matrix){
		
		return new float[1];
	}

	static public float[] getCombinedArray(Complex[][] matrix){
		
		return new float[1];
	}*/
}