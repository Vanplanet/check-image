package com.saferide;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;

public class Detection {

    static class BBox {
        public Rect getBox() {
            return box;
        }

        public void setBox(Rect box) {
            this.box = box;
        }

        public float getConfidence() {
            return confidence;
        }

        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        Rect box = new Rect();
        float confidence;
        int index;
    }

    static String pro_dir = "F:/crop_paper";   // 根路径
    // 配置 权重 图片路径 类别文件
    static String modelConfiguration = pro_dir + "/cfg/yolov3-voc.cfg"; // 模型配置文件
    static String modelWeights = pro_dir + "/weight/yolov3-voc_3000.weights"; // 模型权重文件
    static String classesFile = pro_dir + "/names/voc.names"; // 模型可识别类别的标签文件
    static Net net = null;

    static float confThreshold = 0.3f;  // 置信度阈值
    static float nmsThreshold = 0.4f;   // iou阈值
    static int inpWidth = 416;          // 修改输入图片的宽高
    static int inpHeight = 416;
    static List<String> classes = new ArrayList<String>();  // 存放类别的列表

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // 配置 权重 图片路径 类别文件
        modelConfiguration = pro_dir + "/cfg/yolov3-voc.cfg"; // 模型配置文件
        modelWeights = pro_dir + "/weight/yolov3-voc_3000.weights"; // 模型权重文件
        classesFile = pro_dir + "/names/voc.names"; // 模型可识别类别的标签文件
        //加载
        net = Dnn.readNetFromDarknet(modelConfiguration, modelWeights);
        //简化配置环境变量
        net.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
        //采用opencl运行加速，使其只能运行在inter的gpu上
        net.setPreferableTarget(Dnn.DNN_TARGET_OPENCL);
    }

    public static void main(String[] args) throws Exception {
        String image = "eddcb31518fa4d71a2f1f12505afd28d.jpg";
        detect(image);
    }

    public static int detect(String image) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String image_path = pro_dir + "/image/" + image; // 图片路径，名字

        // 进入识别图片的方法
        int resultNum = detect_image(image_path, image);
        return resultNum;
    }

    //    检测图片
    public static int detect_image(String image_path, String image) throws Exception {

        // 读取classesFile路径的文件
        InputStream in = new FileInputStream(classesFile);
        int iAvail = in.available(); // 适用于本地一次读取多个字节时，返回得到的字节数。
        byte[] bytes = new byte[iAvail];
        in.read(bytes);
        String allContent = new String(bytes); // 文件中的所有内容
        String[] tempContent = allContent.trim().split("\n"); // allContent去除首尾空格，再按换行符分割。
//        System.out.println(tempContent.length);

        // 遍历tempContent，添加到保存类别名的列表classes里。
        for (int i = 0; i < tempContent.length; i++) {
            classes.add(tempContent[i]);
        }
//        System.out.println(classes.size());

        Mat im = imread(image_path, Imgcodecs.IMREAD_COLOR); // 读入待检测的图片

        // 当前是以窗体的形式查看检测后的图片，给窗体命名
//        final String kWinName = "Deep object detection in OpenCV";
//        namedWindow(kWinName, WINDOW_NORMAL);

        // 将输入图片的宽高重新设置 (416, 416)
        Mat frame = new Mat();
        Size sz1 = new Size(im.cols(), im.rows());
        Imgproc.resize(im, frame, sz1);
        Mat resized = new Mat();
        Size sz = new Size(inpWidth, inpHeight);
        Imgproc.resize(im, resized, sz);

        float scale = 1.0F / 255.0F; // 像素归一化

        List<String> ln = net.getLayerNames(); // 获得YOLO各层的名字
        List<String> x = new ArrayList<String>();
        List<List<Integer>> out = new ArrayList<List<Integer>>(); // 存放的是列表
        List<Integer> temp = net.getUnconnectedOutLayers().toList(); // 获得未连接的输出层的索引列表
        out.add(temp);
        // out中存放的是一个List ,get(0)得到的就是list i 索引列表
        List<Integer> i = out.get(0);
//        System.out.println(i.size()); // 3
        for (int a = 0; a < i.size(); a++) {
            String n = ln.get(i.get(a) - 1); // 输出层的名字
            x.add(n); // 找到所有的输出层
        }
        ln = x; // 给ln重新赋值，为输出层名称
        System.out.println(ln); // [yolo_82, yolo_94, yolo_106]

        // 矩阵列表 [Mat[...], Mat[...], Mat[...]]
        List<Mat> outs = new ArrayList<Mat>();

        Mat blob = Dnn.blobFromImage(im, scale, sz, new Scalar(0), true, false);
        net.setInput(blob);

        net.forward(outs, ln); // ln此时为输出层的名字列表，向前传播，将得到的检测结果传入outs
        System.out.println(outs);//32位单精度浮点型单通道图像

        // 进入检测识别方法
        int resultNum = postprocess_(im, outs, image);

        return resultNum;

        // 检测结束后显示图片
//        imshow(kWinName, im); // 显示图片
//        waitKey(300000);
//        waitKey(1);
    } // detect_image 方法结束9

    public static int postprocess_(Mat im, List<Mat> outs, String image) {

        System.out.println("检测过程开始");
        List<Rect> boxes = new ArrayList<Rect>(); // 矩形框列表
        List<Integer> classIds = new ArrayList<Integer>(); // 类的序号列表
        List<Float> confidences = new ArrayList<Float>(); // 置信度列表

        // 循环List<Mat>
        for (int i = 0; i < outs.size(); i++) {
            Mat mat = outs.get(i);
            // 循环每一个mat对象
            // 按行循环
            for (int j = 0; j < mat.rows(); j++) {
                int probaility_index = 5; // [x,y,h,w,c,class1,class2] 所以是标号5
                int size = (int) (mat.cols() * mat.channels());
                float[] data = new float[size];
                mat.get(j, 0, data);
                float confidence = -1;
                int classId = -1;
                // 按列循环
                for (int k = 0; k < mat.cols(); k++) {
                    // 相当于[5:] np.argmax(scores)
                    if (k >= probaility_index && confidence < data[k]) {
                        confidence = data[k]; // 最大值
                        classId = k - probaility_index; // 得到检测得的类别索引
                    }
                }

                // 过滤掉置信度较小的检测结果
                if (confidence > 0.3) {
//                    System.out.println("Result  Object:" + j);
                    for (int k = 0; k < mat.cols(); k++) {
//                        System.out.println(" " + k + ":" + data[k]);
                    }
//                    System.out.println("");
                    float x = data[0]; // centerX 矩形中心点的X坐标
                    float y = data[1]; // centerY 矩形中心点的Y坐标
                    float width = data[2]; // 矩形框的宽
                    float height = data[3]; //矩形框的高
                    float xLeftBottom = (x - width / 2) * im.cols(); // 矩形左下角点的X坐标
                    float yLeftBottom = (y - height / 2) * im.rows(); // 矩形左下角点的Y坐标
                    float xRightTop = (x + width / 2) * im.cols(); // 矩形右上角点的X坐标
                    float yRightTop = (y + height / 2) * im.rows(); // 矩形右上角点的Y坐标

                    // boxes列表填值 Rect对象，参数是两个点
                    boxes.add(new Rect(new Point(xLeftBottom, yLeftBottom), new Point(xRightTop, yRightTop)));
                    confidences.add(confidence);
                    classIds.add(classId);
                }
            }

        }

        System.out.println(classIds);
        System.out.println(confidences);
        System.out.println(boxes.size());
        System.out.println(boxes);

        // 非极大值抑制
        List<Integer> indices = new ArrayList<Integer>();
        // 此处的非极大值抑制为自己重写的方法。
        NMSBoxes(boxes, confidences, confThreshold, nmsThreshold, indices);
        int a = 0;
        // indices : 最终剩下的按置信度由高到低的矩形框序号
        if (indices.size() > 0) {
            for (int b = 0; b < indices.size(); b++) {
                a = a + 1;
                Rect box = boxes.get(indices.get(b));
                Point p1 = box.tl(); // 获得左 上角点
                Point p2 = box.br(); // 获得右下角点
                int classId = classIds.get(a - 1); // 得到类别序号
                float confidence = confidences.get(a - 1); // 得到置信度值
                // 进入画框框方法
                drawPred_(classId, confidence, im, p1, p2);
            }
        }

        Imgcodecs.imwrite("F:/crop_paper/output/" + image, im);
        return boxes.size();
    }

    public static void NMSBoxes(List<Rect> boxes, List<Float> confidences, float confThreshold, float nmsThreshold, List<Integer> indices) {
        // 新建一个List 存放BBox的对象
        List<BBox> bboxes = new ArrayList<BBox>();
        // 循环向bboxes里添加值：Rect(Point,Point) float confidence, int index
        for (int i = 0; i < boxes.size(); i++) {
            BBox bbox = new BBox();
            bbox.box = boxes.get(i);
            bbox.confidence = confidences.get(i);
            bbox.index = i;
            bboxes.add(bbox);
        }

        // 排序算法 根据confidence的属性值，降序排列List bboxes
        for (int i = 0; i < bboxes.size(); i++) {
            for (int j = 0; j < bboxes.size() - i - 1; j++) {
                if (bboxes.get(j).confidence < bboxes.get(j + 1).confidence) {
                    BBox temp = bboxes.get(j);
                    bboxes.set(j, bboxes.get(j + 1));
                    bboxes.set(j + 1, temp);
                }
            }
        }

        int updated_size = bboxes.size();
        // 循环删除
        for (int i = 0; i < updated_size; i++) {
            // 如果list中的box置信度小于阈值，跳出循环
            if (bboxes.get(i).confidence < confThreshold) {
                continue;
            }
            // indices存放最终剩下的按置信度由高到低的矩形框的序列号
            indices.add(bboxes.get(i).index);

            // 比较第一个值（ 最大值）与后面每一个值的交并比
            // 如果iou>阈值，删除这个框,更改bboxes的长度
            for (int j = i + 1; j < updated_size; j++) {
                float iou = getIouValue(bboxes.get(i).box, bboxes.get(j).box);
                if (iou > nmsThreshold) {
                    bboxes.remove(j);
                    updated_size = bboxes.size();
                }
            }
        }
    }

    public static float getIouValue(Rect rect1, Rect rect2) {
        int xx1, yy1, xx2, yy2;
        xx1 = Math.max(rect1.x, rect2.x);
        yy1 = Math.max(rect1.y, rect2.y);
        xx2 = Math.min(rect1.x + rect1.width - 1, rect2.x + rect2.width - 1);
        yy2 = Math.min(rect1.y + rect1.height - 1, rect2.y + rect2.height - 1);

        int insection_width, insection_height;
        insection_width = Math.max(0, xx2 - xx1 + 1);
        insection_height = Math.max(0, yy2 - yy1 + 1);

        float insection_area, union_area, iou;
        insection_area = insection_width * insection_height;
        union_area = rect1.width * rect1.height + rect2.width * rect2.height - insection_area;
        iou = insection_area / union_area;

        return iou;
    }

    public static void drawPred_(int classId, float confidence, Mat im, Point p1, Point p2) {
        String text;
        double x = p1.x; // p1 的 x 坐标
        double y = p1.y; // p1 的 y 坐标

        // 下面加if语句只是为了区分人和其他类别的不同颜色，改成随机获取颜色也可以
        if (classId == 0) {
//            System.out.println("1");
            rectangle(im, p1, p2, new Scalar(0, 0, 255), 3);
            text = classes.get(classId) + ":" + confidence;
            putText(im, text, new Point(x, y - 5), FONT_HERSHEY_SIMPLEX, 0.3, new Scalar(0, 255, 0), 1);
        } else {
//            System.out.println("2");
            rectangle(im, p1, p2, new Scalar(0, 255, 0), 3); // 画框
            text = String.format("%s %f", classes.get(classId), confidence); // 标签内容
            System.out.println(text);
            // 把标签添加到矩形框左上
            putText(im, text, new Point(x, y - 5), FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 1);
        }
    }
}
