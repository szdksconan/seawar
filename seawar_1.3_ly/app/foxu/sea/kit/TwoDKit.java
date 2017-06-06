package foxu.sea.kit;

import java.awt.Point;

/**
 * 2d ���߰�
 * @author yw
 *
 */
public class TwoDKit
{

	public static double determinant(double v1,double v2,double v3,double v4) // ����ʽ
	{
		return (v1*v3-v2*v4);
	}

	public static boolean ContainSame(Point a,Point b,Point c,Point d)
	{
		if((a.x==c.x&&a.y==c.y)||(a.x==d.x&&a.y==d.y)||(b.x==c.x&&b.y==c.y)
			||(b.x==d.x&&b.y==d.y)) return true;
		return false;
	}

	/** ����߶��Ƿ��ཻ  2D*/
	public static boolean intersect(Point a,Point b,Point c,Point d)
	{
		double delta=determinant(b.x-a.x,c.x-d.x,b.y-a.y,c.y-d.y);
		if(delta<=(1e-6)&&delta>=-(1e-6)) // delta=0����ʾ���߶��غϻ�ƽ��
		{
			return ContainSame(a,b,c,d);
		}
		double namenda=determinant(c.x-a.x,c.x-d.x,c.y-a.y,c.y-d.y)/delta;
		if(namenda>1||namenda<0)
		{
			return false;
		}
		double miu=determinant(b.x-a.x,c.x-a.x,b.y-a.y,c.y-a.y)/delta;
		if(miu>1||miu<0)
		{
			return false;
		}
		return true;
	}


}
