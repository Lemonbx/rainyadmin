<template>
    <div class="file-upload">
        <n-upload
            v-model:file-list="internalFileList"
            :action="uploadUrl"
            :headers="headers"
            :data="uploadData"
            :accept="accept"
            :max="maxFiles"
            :multiple="maxFiles > 1"
            :show-file-list="false"
            :on-before-upload="handleBeforeUpload"
            :on-finish="handleFinish"
            :on-error="handleError"
            :on-remove="handleRemove"
            :on-progress="handleProgress"
        >
            <div class="upload-area">
                <div v-if="uploading" class="upload-progress">
                    <n-progress type="line" :percentage="uploadPercent" :show-indicator="true" height="8" />
                </div>
                <div v-else-if="!hasFiles" class="upload-placeholder">
                    <n-icon size="48" color="#d9d9d9">
                        <CloudUploadOutline />
                    </n-icon>
                    <p class="upload-text">{{ placeholder }}</p>
                </div>
                <div v-else class="files-preview">
                    <div v-for="(file, index) in displayFiles" :key="index" class="file-item">
                        <div class="file-preview">
                            <img v-if="isImageFile(file)" :src="file" class="preview-image" />
                            <video v-else-if="isVideoFile(file)" :src="file" class="preview-video" controls />
                            <div v-else class="file-info">
                                <n-icon size="24">
                                    <DocumentOutline />
                                </n-icon>
                                <span>{{ getFileName(file) }}</span>
                            </div>
                            <div class="upload-actions">
                                <n-button size="small" type="error" @click.stop="removeFile(index)">
                                    删除
                                </n-button>
                            </div>
                        </div>
                    </div>
                    <div v-if="maxFiles > 1 && displayFiles.length < maxFiles" class="add-more">
                        <div class="add-more-placeholder">
                            <n-icon size="24" color="#d9d9d9">
                                <CloudUploadOutline />
                            </n-icon>
                            <span>添加更多</span>
                        </div>
                    </div>
                </div>
            </div>
        </n-upload>
    </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useMessage } from 'naive-ui'
import { CloudUploadOutline, DocumentOutline } from '@vicons/ionicons5'
import request from '@/utils/request'

const props = defineProps({
    value: {
        type: String,
        default: ''
    },
    type: {
        type: String,
        default: 'image', // image, video, file
        validator: (value) => ['image', 'video', 'file'].includes(value)
    },
    placeholder: {
        type: String,
        default: '点击上传文件'
    },
    maxSize: {
        type: Number,
        default: 1024 * 1024 * 1024 // 1GB
    },
    maxFiles: {
        type: Number,
        default: 1
    }
})

const emit = defineEmits(['update:value', 'change'])
const message = useMessage()

// naiveui的受控文件列表
const internalFileList = ref([])
const uploading = ref(false)
const uploadPercent = ref(0)

// 显示的文件列表（从value解析出来的URL列表）
const displayFiles = computed(() => {
    if (!props.value) return []
    return props.value.split(',').filter(url => url.trim())
})

// 是否有文件
const hasFiles = computed(() => {
    return displayFiles.value.length > 0
})

// 上传配置
const uploadUrl = computed(() => {
    return `${request.defaults.baseURL}/upload/file`
})

const headers = computed(() => {
    return {
        'auth': localStorage.getItem('token') || ''
    }
})

const uploadData = computed(() => {
    return {
        type: props.type
    }
})

const accept = computed(() => {
    switch (props.type) {
        case 'image':
            return 'image/*'
        case 'video':
            return 'video/*'
        default:
            return '*/*'
    }
})

// 文件类型判断
const isImageFile = (url) => {
    return props.type === 'image' && url
}

const isVideoFile = (url) => {
    return props.type === 'video' && url
}

const getFileName = (url) => {
    if (!url) return ''
    return url.split('/').pop()
}

// 更新value的方法
const updateValue = (urls) => {
    const newValue = urls.filter(url => url).join(',')
    if (newValue !== props.value) {
        emit('update:value', newValue)
        emit('change', newValue)
    }
}

// 移除文件
const removeFile = (index) => {
    const newFiles = [...displayFiles.value]
    newFiles.splice(index, 1)
    updateValue(newFiles)
}

// 上传前检查
const handleBeforeUpload = ({ file }) => {
    if (file.file.size > props.maxSize) {
        message.error(`文件大小不能超过 ${props.maxSize / 1024 / 1024}MB`)
        return false
    }

    if (displayFiles.value.length >= props.maxFiles) {
        message.error(`最多只能上传 ${props.maxFiles} 个文件`)
        return false
    }

    uploading.value = true
    uploadPercent.value = 0
    return true
}

// 上传进度
const handleProgress = ({ percent }) => {
    uploading.value = true
    uploadPercent.value = Math.floor(percent)
}

// 上传完成
const handleFinish = ({ file, event }) => {
    uploading.value = false
    uploadPercent.value = 0

    try {
        const responseText = event?.target?.responseText || event?.currentTarget?.responseText
        if (responseText) {
            const response = JSON.parse(responseText)
            if (response && (response.code === 1 || response.data)) {
                const uploadedUrl = response.data
                const currentFiles = displayFiles.value

                if (props.maxFiles === 1) {
                    // 单文件模式，替换
                    updateValue([uploadedUrl])
                } else {
                    // 多文件模式，追加
                    updateValue([...currentFiles, uploadedUrl])
                }

                message.success('上传成功')
            } else {
                message.error(response?.msg || '上传失败')
            }
        } else {
            message.error('上传失败')
        }
    } catch (error) {
        console.error('Upload response parse error:', error)
        message.error('上传失败')
    }

    // 清理内部文件列表
    nextTick(() => {
        internalFileList.value = []
    })
}

// 上传错误
const handleError = (error) => {
    uploading.value = false
    uploadPercent.value = 0
    message.error('上传失败')
    console.error('Upload error:', error)

    // 清理内部文件列表
    nextTick(() => {
        internalFileList.value = []
    })
}

// 文件移除（naiveui内部触发）
const handleRemove = ({ file }) => {
    // 这里可以处理从naiveui内部移除文件的逻辑
    // 但我们主要通过自定义的removeFile方法来处理
    return true
}
</script>

<style scoped>
.file-upload {
    width: 100%;
}

.upload-area {
    border: 2px dashed #bdbdbd;
    border-radius: 6px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.3s;
    min-height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.upload-area:hover {
    border-color: #1890ff;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.upload-text {
    margin: 0;
    color: #666;
    font-size: 14px;
}

.files-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
    width: 100%;
}

.file-item {
    position: relative;
    display: inline-block;
}

.file-preview {
    position: relative;
    display: inline-block;
}

.preview-image {
    max-width: 150px;
    max-height: 150px;
    border-radius: 4px;
    object-fit: cover;
}

.preview-video {
    max-width: 200px;
    max-height: 150px;
    border-radius: 4px;
}

.file-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 4px;
    min-width: 120px;
    max-width: 150px;
}

.file-info span {
    font-size: 12px;
    color: #666;
    word-break: break-all;
}

.upload-actions {
    position: absolute;
    top: 4px;
    right: 4px;
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.3s;
}

.file-preview:hover .upload-actions {
    opacity: 1;
}

.upload-progress {
    width: 100%;
    max-width: 300px;
}

.add-more {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 120px;
    min-height: 120px;
    border: 2px dashed #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    transition: border-color 0.3s;
}

.add-more:hover {
    border-color: #1890ff;
}

.add-more-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #999;
    font-size: 12px;
}
</style>
