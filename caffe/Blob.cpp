template <typename Dtype>
Blob {

shared_ptr<SyncedMemory> data_, diff_, shape_data_
vector<int> shape_
int count, capacity

Blob(int num, channels, height, width)  // num指第几张图像
Blob(const vector<int>& shape)

const Dtype* cpu_data() const
void set_cpu_data(Dtype* data) {
    CHECK(data)
    data_->set_cpu_data(data)
}

}