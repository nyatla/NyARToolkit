package jp.nyatla.nyartoolkit.core.kpm;

public class VisualDatabaseFacade {
    class VisualDatabaseImpl{
    public VisualDatabaseImpl(){
    	mVdb.reset(new vdb_t());
    }
        std::unique_ptr<vdb_t> mVdb;
        point3d_map_t mPoint3d;
    };
    
    VisualDatabaseFacade::VisualDatabaseFacade(){
        mVisualDbImpl.reset(new VisualDatabaseImpl());
    }
    VisualDatabaseFacade::~VisualDatabaseFacade(){
        
    }
    
    void VisualDatabaseFacade::addImage(unsigned char* grayImage,
                                        size_t width,
                                        size_t height,
                                        int image_id) {
        Image img;
        img.deepCopy(Image(grayImage,IMAGE_UINT8,width,height,(int)width,1));
        mVisualDbImpl->mVdb->addImage(img, image_id);
    }
    
    void VisualDatabaseFacade::addFreakFeaturesAndDescriptors(const std::vector<FeaturePoint>& featurePoints,
                                                              const std::vector<unsigned char>& descriptors,
                                                              const std::vector<vision::Point3d<float> >& points3D,
                                                              size_t width,
                                                              size_t height,
                                                              int image_id){
        std::shared_ptr<Keyframe<96> > keyframe(new Keyframe<96>());
        keyframe->setWidth((int)width);
        keyframe->setHeight((int)height);
        keyframe->store().setNumBytesPerFeature(96);
        keyframe->store().points().resize(featurePoints.size());
        keyframe->store().points() = featurePoints;
        keyframe->store().features().resize(descriptors.size());
        keyframe->store().features() = descriptors;
        keyframe->buildIndex();
        mVisualDbImpl->mVdb->addKeyframe(keyframe, image_id);
        mVisualDbImpl->mPoint3d[image_id] = points3D;
    }
    
    void VisualDatabaseFacade::computeFreakFeaturesAndDescriptors(unsigned char* grayImage,
                                                                  size_t width,
                                                                  size_t height,
                                                                  std::vector<FeaturePoint>& featurePoints,
                                                                  std::vector<unsigned char>& descriptors){
        Image img = Image(grayImage,IMAGE_UINT8,width,height,(int)width,1);
        std::unique_ptr<vdb_t> tmpDb(new vdb_t());
        tmpDb->addImage(img, 1);
        featurePoints = tmpDb->keyframe(1)->store().points();
        descriptors = tmpDb->keyframe(1)->store().features();
    }
    
    bool VisualDatabaseFacade::query(unsigned char* grayImage,
                                     size_t width,
                                     size_t height){
        Image img = Image(grayImage,IMAGE_UINT8,width,height,(int)width,1);
        return mVisualDbImpl->mVdb->query(img);
    }
    
    bool VisualDatabaseFacade::erase(int image_id){
        return mVisualDbImpl->mVdb->erase(image_id);
    }
    
    const size_t VisualDatabaseFacade::databaseCount(){
        return mVisualDbImpl->mVdb->databaseCount();
    }
    
    int VisualDatabaseFacade::matchedId(){
        return mVisualDbImpl->mVdb->matchedId();
    }
    
    const float* VisualDatabaseFacade::matchedGeometry(){
        return mVisualDbImpl->mVdb->matchedGeometry();
    }
    
    const std::vector<FeaturePoint> &VisualDatabaseFacade::getFeaturePoints(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->store().points();
    }
    
    const std::vector<unsigned char> &VisualDatabaseFacade::getDescriptors(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->store().features();
    }
    
    const std::vector<vision::Point3d<float> >& VisualDatabaseFacade::get3DFeaturePoints(int image_id) const{
        return mVisualDbImpl->mPoint3d[image_id];
    }
    
    const std::vector<FeaturePoint>&VisualDatabaseFacade::getQueryFeaturePoints() const{
        return mVisualDbImpl->mVdb->queryKeyframe()->store().points();
    }
    
    const std::vector<unsigned char>&VisualDatabaseFacade::getQueryDescriptors() const{
        return mVisualDbImpl->mVdb->queryKeyframe()->store().features();
    }
    
    const matches_t& VisualDatabaseFacade::inliers() const{
        return mVisualDbImpl->mVdb->inliers();
    }
    
    int VisualDatabaseFacade::getWidth(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->width();
    }
    int VisualDatabaseFacade::getHeight(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->height();
    }
}
