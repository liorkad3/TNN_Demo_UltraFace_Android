#ifndef TNNDEMO_ULTRAFACE_H
#define TNNDEMO_ULTRAFACE_H

#include <vector>
#include <tnn/utils/blob_converter.h>
#include <tnn/core/tnn.h>

#define num_featuremap 4
#define hard_nms 1
#define blending_nms 2 /* mix nms was been proposaled in paper blaze face, aims to minimize the temporal jitter*/

typedef enum {
    // run on cpu
    TNNComputeUnitsCPU = 0,
    // run on gpu, if failed run on cpu
    TNNComputeUnitsGPU = 1,
    // run on npu, if failed run on cpu
    TNNComputeUnitsNPU = 2,
} TNNComputeUnits;

typedef struct FaceInfo {
    float x1;
    float y1;
    float x2;
    float y2;
    float score;

    float *landmarks = nullptr;
} FaceInfo;

std::vector<FaceInfo> AdjustFaceInfoToOriginalSize(std::vector<FaceInfo> face_info,
                                                   int detect_image_height, int detect_image_width,
                                                   int orig_image_height, int orig_image_width);

class UltraFace {
public:
    ~UltraFace();
    UltraFace(const std::string& modelPath, int input_width, int input_height,
            int computeType = TNNComputeUnitsCPU, int num_thread_ = 4,
            float score_threshold_ = 0.7, float iou_threshold_ = 0.3, int topk_ = -1);

    int initModel(const std::string& modelPath, int computeType);

    int detect(std::shared_ptr<TNN_NS::Mat> image, int image_height, int image_width, std::vector<FaceInfo> &face_list);

private:
    void GenerateBBox(std::vector<FaceInfo> &bbox_collection, TNN_NS::Mat &scores, TNN_NS::Mat &boxes);

    void NMS(std::vector<FaceInfo> &input, std::vector<FaceInfo> &output, int type = blending_nms);

private:
    std::shared_ptr<TNN_NS::TNN> net = nullptr;
    std::shared_ptr<TNN_NS::Instance> instance = nullptr;
    TNN_NS::DeviceType deviceType = TNN_NS::DEVICE_ARM;

    int num_thread;
    int image_w = 0;
    int image_h = 0;

    int in_w;
    int in_h;
    int num_anchors;

    int topk;
    float score_threshold;
    float iou_threshold;

    const std::string model_name = "version-slim-320_simplified";
    const float mean_vals[3] = {127, 127, 127};
    const float norm_vals[3] = {1.0 / 128, 1.0 / 128, 1.0 / 128};

    const float center_variance = 0.1;
    const float size_variance = 0.2;
    const std::vector<std::vector<float>> min_boxes = {
            {10.0f,  16.0f,  24.0f},
            {32.0f,  48.0f},
            {64.0f,  96.0f},
            {128.0f, 192.0f, 256.0f}};
    const std::vector<float> strides = {8.0, 16.0, 32.0, 64.0};
    std::vector<std::vector<float>> featuremap_size;
    std::vector<std::vector<float>> shrinkage_size;
    std::vector<int> w_h_list;

    std::vector<std::vector<float>> priors = {};
};


#endif //TNNDEMO_ULTRAFACE_H
