package com.eeka.mespad.bo;

import java.io.Serializable;
import java.util.List;

public class PostBatchRecordLabuBo implements Serializable {

    /**
     * shopOrderRef : ShopOrderBO:8081,000008000044
     * item : MRL0180001P821
     * layOutRef : ZLayoutBO:000008000044_MRL0180001P821_1.cut:000008000044
     * rabOrderNo : 000008000044001
     * rabSeq : 1
     * materialType : M
     * bulkRabSegments : [{"cutNum":1,"standLength":1,"actualLenth":1.1,"layers":1},{"cutNum":2,"standLength":1,"actualLenth":1.2,"layers":1}]
     * bulkRabRolls : [{"volumn":1,"length":2,"leftNum":1,"shortNum":1,"bulkRabSegments":[{"cutNum":1,"standLength":1,"actualLenth":1.1,"layers":1},{"cutNum":2,"standLength":1,"actualLenth":1.2,"layers":1}]},{"volumn":2,"length":2,"leftNum":1,"shortNum":1,"bulkRabSegments":[{"cutNum":1,"standLength":1,"actualLenth":1.1,"layers":1},{"cutNum":2,"standLength":1,"actualLenth":1.2,"layers":1}]}]
     */

    private String operation;
    private String shopOrderRef;
    private String item;
    private String layOutRef;
    private String layOutName;
    private String layoutNo;
    private String rabOrderNo;
    private String rabSeq;
    private String materialType;
    private String layoutImgUrl;
    private List<CutSizeBean> cutSizes;
    private List<BulkRabRollsBean.BulkRabSegmentsBean> bulkRabSegments;
    private List<BulkRabRollsBean> bulkRabRolls;

    public String getLayOutName() {
        return layOutName;
    }

    public void setLayOutName(String layOutName) {
        this.layOutName = layOutName;
    }

    public List<CutSizeBean> getCutSizes() {
        return cutSizes;
    }

    public void setCutSizes(List<CutSizeBean> cutSizes) {
        this.cutSizes = cutSizes;
    }

    public String getLayoutNo() {
        return layoutNo;
    }

    public void setLayoutNo(String layoutNo) {
        this.layoutNo = layoutNo;
    }

    public String getLayoutImgUrl() {
        return layoutImgUrl;
    }

    public void setLayoutImgUrl(String layoutImgUrl) {
        this.layoutImgUrl = layoutImgUrl;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getShopOrderRef() {
        return shopOrderRef;
    }

    public void setShopOrderRef(String shopOrderRef) {
        this.shopOrderRef = shopOrderRef;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getLayOutRef() {
        return layOutRef;
    }

    public void setLayOutRef(String layOutRef) {
        this.layOutRef = layOutRef;
    }

    public String getRabOrderNo() {
        return rabOrderNo;
    }

    public void setRabOrderNo(String rabOrderNo) {
        this.rabOrderNo = rabOrderNo;
    }

    public String getRabSeq() {
        return rabSeq;
    }

    public void setRabSeq(String rabSeq) {
        this.rabSeq = rabSeq;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public List<BulkRabRollsBean.BulkRabSegmentsBean> getBulkRabSegments() {
        return bulkRabSegments;
    }

    public void setBulkRabSegments(List<BulkRabRollsBean.BulkRabSegmentsBean> bulkRabSegments) {
        this.bulkRabSegments = bulkRabSegments;
    }

    public List<BulkRabRollsBean> getBulkRabRolls() {
        return bulkRabRolls;
    }

    public void setBulkRabRolls(List<BulkRabRollsBean> bulkRabRolls) {
        this.bulkRabRolls = bulkRabRolls;
    }

    public static class BulkRabRollsBean implements Serializable {
        /**
         * volumn : 1
         * length : 2.0
         * leftNum : 1.0
         * shortNum : 1.0
         * bulkRabSegments : [{"cutNum":1,"standLength":1,"actualLenth":1.1,"layers":1},{"cutNum":2,"standLength":1,"actualLenth":1.2,"layers":1}]
         */

        private String volumn;
        private String length;
        private String leftNum;
        private String shortNum;
        private List<BulkRabSegmentsBean> bulkRabSegments;

        public String getVolumn() {
            return volumn;
        }

        public void setVolumn(String volumn) {
            this.volumn = volumn;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public String getLeftNum() {
            return leftNum;
        }

        public void setLeftNum(String leftNum) {
            this.leftNum = leftNum;
        }

        public String getShortNum() {
            return shortNum;
        }

        public void setShortNum(String shortNum) {
            this.shortNum = shortNum;
        }

        public List<BulkRabSegmentsBean> getBulkRabSegments() {
            return bulkRabSegments;
        }

        public void setBulkRabSegments(List<BulkRabSegmentsBean> bulkRabSegments) {
            this.bulkRabSegments = bulkRabSegments;
        }

        public static class BulkRabSegmentsBean implements Serializable {
            /**
             * cutNum : 1
             * standLength : 1.0
             * actualLenth : 1.1
             * layers : 1
             */

            private int cutNum;
            private String standLength;
            private String actualLenth;
            private String layers;

            public int getCutNum() {
                return cutNum;
            }

            public void setCutNum(int cutNum) {
                this.cutNum = cutNum;
            }

            public String getStandLength() {
                return standLength;
            }

            public void setStandLength(String standLength) {
                this.standLength = standLength;
            }

            public String getActualLenth() {
                return actualLenth;
            }

            public void setActualLenth(String actualLenth) {
                this.actualLenth = actualLenth;
            }

            public String getLayers() {
                return layers;
            }

            public void setLayers(String layers) {
                this.layers = layers;
            }
        }
    }

    public static class CutSizeBean {
        private int cutNum;
        private String sizeCode;
        private int layers;

        public int getLayers() {
            return layers;
        }

        public void setLayers(int layers) {
            this.layers = layers;
        }

        public int getCutNum() {
            return cutNum;
        }

        public void setCutNum(int cutNum) {
            this.cutNum = cutNum;
        }

        public String getSizeCode() {
            return sizeCode;
        }

        public void setSizeCode(String sizeCode) {
            this.sizeCode = sizeCode;
        }
    }
}
