template <typename Dtype>
Layer {

Layer(const LayerParameter& param)
    : layer_param_(param), is_share_(false) {
    phase_ = param.phase()
    const int size = param.blobs_size()
    if size > 0
        blobs_.resize(size)
        for i = [0, size)
            blobs_[i]->reset(new Blob<Dtype>())
            blobs_[i]->FromProto(param.blobs(i))
}

void Setup(const vector<Blob<Dtype>*>& bottom, top)

}