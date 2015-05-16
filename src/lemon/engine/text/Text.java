package lemon.engine.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import lemon.engine.math.Vector;
import lemon.render.ModelData;

public class Text {
	private Font font;
	private String text;
	private BufferedImage image;
	private boolean[][] binaryData;
	private ModelData modelData;
	public Text(Font font, String text){
		this.text = text;
		//Image
		BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D dummyG = dummy.createGraphics();
		dummyG.setFont(font);
		Rectangle2D rect = dummyG.getFontMetrics().getStringBounds(text, dummyG);
		dummyG.dispose();
		image = new BufferedImage((int)rect.getWidth(), (int)rect.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = image.createGraphics();
		g.setFont(font);
		g.drawString(text, 0, (int)(-rect.getY()));
		g.dispose();
		//ModelData
		modelData = new ModelData(new int[]{3, 2, 3});
		binaryData = new boolean[image.getWidth()][image.getHeight()];
		boolean[][] tempData = new boolean[binaryData.length][binaryData[0].length];
		for(int i=0;i<image.getWidth();++i){
			for(int j=0;j<image.getHeight();++j){
				binaryData[i][j] = image.getRGB(i, j)==-1;
				tempData[i][j] = binaryData[i][j];
			}
		}
		int[][] frontLineData = new int[binaryData.length+1][binaryData[0].length+1];
		int[][] backLineData = new int[binaryData.length+1][binaryData[0].length+1];
		for(int i=0;i<frontLineData.length;++i){
			for(int j=0;j<frontLineData[0].length;++j){
				frontLineData[i][j] = -1;
				backLineData[i][j] = -1;
			}
		}
		for(int i=0;i<tempData.length;++i){
			for(int j=0;j<tempData[0].length;++j){
				if(binaryData[i][j]){
					if(frontLineData[i][j]==-1){
						Vector normal = getNormal(binaryData, i, j, 1);
						modelData.addData(((float)i)-(((float)(binaryData.length))/2f),
								1f-((float)j), 0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						frontLineData[i][j] = modelData.getSize()-1;
					}
					if(frontLineData[i+1][j]==-1){
						Vector normal = getNormal(binaryData, i+1, j, 1);
						modelData.addData(((float)(i+1))-(((float)(binaryData.length))/2f),
								1f-((float)j), 0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						frontLineData[i+1][j] = modelData.getSize()-1;
					}
					if(frontLineData[i][j+1]==-1){
						Vector normal = getNormal(binaryData, i, j+1, 1);
						modelData.addData(((float)i)-(((float)(binaryData.length))/2f),
								1f-((float)(j+1)), 0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						frontLineData[i][j+1] = modelData.getSize()-1;
					}
					if(frontLineData[i+1][j+1]==-1){
						Vector normal = getNormal(binaryData, i+1, j+1, 1);
						modelData.addData(((float)(i+1))-(((float)(binaryData.length))/2f),
								1f-((float)(j+1)), 0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						frontLineData[i+1][j+1] = modelData.getSize()-1;
					}
					if(backLineData[i][j]==-1){
						Vector normal = getNormal(binaryData, i, j, -1);
						modelData.addData(((float)i)-(((float)(binaryData.length))/2f),
								1f-((float)j), -0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						backLineData[i][j] = modelData.getSize()-1;
					}
					if(backLineData[i+1][j]==-1){
						Vector normal = getNormal(binaryData, i+1, j, -1);
						modelData.addData(((float)(i+1))-(((float)(binaryData.length))/2f),
								1f-((float)j), -0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						backLineData[i+1][j] = modelData.getSize()-1;
					}
					if(backLineData[i][j+1]==-1){
						Vector normal = getNormal(binaryData, i, j+1, -1);
						modelData.addData(((float)i)-(((float)(binaryData.length))/2f),
								1f-((float)(j+1)), -0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						backLineData[i][j+1] = modelData.getSize()-1;
					}
					if(backLineData[i+1][j+1]==-1){
						Vector normal = getNormal(binaryData, i+1, j+1, -1);
						modelData.addData(((float)(i+1))-(((float)(binaryData.length))/2f),
								1f-((float)(j+1)), -0.5f, 0f, 0f, normal.getX(), normal.getY(), normal.getZ());
						backLineData[i+1][j+1] = modelData.getSize()-1;
					}
					modelData.addIndices(frontLineData[i][j], frontLineData[i][j+1], frontLineData[i+1][j+1],
							frontLineData[i][j], frontLineData[i+1][j+1], frontLineData[i+1][j]);
					modelData.addIndices(backLineData[i][j], backLineData[i][j+1], backLineData[i+1][j+1],
							backLineData[i][j], backLineData[i+1][j+1], backLineData[i+1][j]);
					if(!exists(binaryData, i-1, j)){
						modelData.addIndices(frontLineData[i][j], backLineData[i][j], backLineData[i][j+1],
								frontLineData[i][j], backLineData[i][j+1], frontLineData[i][j+1]);
					}
					if(!exists(binaryData, i+1, j)){
						modelData.addIndices(frontLineData[i+1][j], backLineData[i+1][j], backLineData[i+1][j+1],
								frontLineData[i+1][j], backLineData[i+1][j+1], frontLineData[i+1][j+1]);
					}
					if(!exists(binaryData, i, j-1)){
						modelData.addIndices(frontLineData[i][j], backLineData[i][j], backLineData[i+1][j],
								frontLineData[i][j], backLineData[i+1][j], frontLineData[i+1][j]);
					}
					if(!exists(binaryData, i, j+1)){
						modelData.addIndices(frontLineData[i][j+1], backLineData[i][j+1], backLineData[i+1][j+1],
								frontLineData[i][j+1], backLineData[i+1][j+1], frontLineData[i+1][j+1]);
					}
				}
			}
		}
	}
	private Vector getNormal(boolean[][] binaryData, int x, int y, int normalZ){
		boolean a = exists(binaryData, x, y);
		boolean b = exists(binaryData, x+1, y);
		boolean c = exists(binaryData, x, y+1);
		boolean d = exists(binaryData, x+1, y+1);
		if(a==b&&b==c&&c==d){
			return new Vector(0, 0, normalZ);
		}
		if(a==d&&b==c){
			return new Vector(0, 0, normalZ);
		}
		if(a&&b&&c){
			return new Vector(1, 1, normalZ);
		}
		if(a&&b&&d){
			return new Vector(-1, 1, normalZ);
		}
		if(a&&c&&d){
			return new Vector(1, -1, normalZ);
		}
		if(b&&c&&d){
			return new Vector(-1, -1, normalZ);
		}
		if(a&&b){
			return new Vector(0, 1, normalZ);
		}
		if(a&&c){
			return new Vector(1, 0, normalZ);
		}
		if(b&&d){
			return new Vector(-1, 0, normalZ);
		}
		if(c&&d){
			return new Vector(0, -1, normalZ);
		}
		if(a){
			return new Vector(1, 1, normalZ);
		}
		if(b){
			return new Vector(-1, 1, normalZ);
		}
		if(c){
			return new Vector(1, -1, normalZ);
		}
		if(d){
			return new Vector(-1, -1, normalZ);
		}
		System.out.println(a+" - "+b+" - "+c+" - "+d);
		return new Vector(0, 0, normalZ);
	}
	private boolean exists(boolean[][] binaryData, int x, int y){
		if(x<0||y<0||x>=binaryData.length||y>=binaryData[0].length){
			return false;
		}
		return binaryData[x][y];
	}
	public Font getFont(){
		return font;
	}
	public ModelData getModelData(){
		return modelData;
	}
	public BufferedImage getImage(){
		return image;
	}
	public String getText(){
		return text;
	}
}
