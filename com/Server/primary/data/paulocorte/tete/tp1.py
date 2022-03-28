import matplotlib
import os
from scipy.fftpack import dct, idct
import math
import matplotlib.pyplot as plt
from PIL import Image
import matplotlib.colors as clr
import numpy as np
import cv2
from tabulate import tabulate

#colormaps 

def plot_debug(ch1, ch2, ch3):
    plt.figure()
    plt.subplot(131)
    plt.imshow(ch1,cmGray)
    plt.subplot(132)
    plt.imshow(ch2, cmGray)
    plt.subplot(133)
    plt.imshow(ch3, cmGray)
    plt.show(block=False)

# 3.2 -used-defined colormap
def color_map(color_map_name, min_color=(0,0,0), max_color = (1,1,1)) :
    return clr.LinearSegmentedColormap.from_list(color_map_name, [min_color,max_color], 256) 

 
cmRed = color_map('myRed', (0,0,0),(1,0,0))
cmGreen = color_map('myGreen', (0,0,0),(0,1,0) )
cmBlue = color_map( 'myBlue', (0,0,0),(0,0,1))

# 3.3 -image visualization with colormap
def view_image(img, color_map,title="<untitled>"):
    plt.figure()
    plt.title(title)
    plt.imshow(img, color_map)
    plt.show(block=False)

#3.4 - channel separation and union
def separate_3channels(image):
    return (image[:,:,0], image[:,:,1], image[:,:,2])

def join_3channels(r,g,b):
    return  np.dstack((r,g,b))

def ex3():
    #3.1 - reading barn_mountains.bmp
    img_bm = plt.imread("imagens/barn_mountains.bmp")

    #3.5 - channel visualization
    r,g,b = separate_3channels(img_bm)
    

    d = {'red':cmRed, 'green': cmGreen, 'blue': cmBlue }
    e = {'red': r, 'green':g, 'blue':b}
    for col in d.keys():
        view_image(e[col],d[col], col)
    return img_bm


def image_padding(img, ds_rate, bs):

    mult=0
    if (ds_rate[2]==0):
        mult=(ds_rate[0]//ds_rate[1]) * bs
    else: 
        mult= (ds_rate[0]// min(ds_rate)) * bs


    sh = or_shape = img.shape
    last_line = img[len(img)-1,:,:]
    if (len(img)%mult!=0):   
        arr_to_add = np.tile(last_line, (mult-len(img)%mult,1)).reshape(mult-len(img)%mult,or_shape[1],3)
        img = np.vstack((img, arr_to_add))

    last_col = np.array([img[:, len(img[0])-1, :]])
    sh= img.shape
    if(sh[1]%mult!=0):
        arr_to_add = np.tile(last_col, (1,mult-sh[1]%mult)).reshape(sh[0], mult-sh[1]%mult,3)
        img = np.hstack((img, arr_to_add))

    return img, or_shape

def image_remove_padding(img, shape):
    h,c = shape[0], shape[1]
    return img[:h,:c,:]

def ex4():
    print("Original image shape: ",img_bm.shape)
    img_bm_padded, original_shape = image_padding(img_bm, (4,2,2), 8)
    print("Padded image shape: ",img_bm_padded.shape)
    img_bm_no_padding = image_remove_padding(img_bm_padded, original_shape)
    print("Image with padding removed shape: ",img_bm_no_padding.shape)
    print("Removal correct? " , np.array_equal(img_bm_no_padding, img_bm))
    return img_bm_padded

def compare_3channels(ch1_1, ch2_1, ch3_1, ch1_2, ch2_2, ch3_2):
    eqs1 = [ch1_1, ch2_1, ch3_1]
    eqs2 = [ch1_2, ch2_2, ch3_2]
    equals=0
    for i in range(len(eqs1)):
        howmany=np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001)
        if howmany==0: 
            equals +=1
        else:
            print('no of different pixels: ',np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001))
            view_image(eqs1[i],cmGray,'original')
            view_image(eqs2[i],cmGray,'encoded')
            view_image(eqs2[i]-eqs1[i],cmGray,'difference')
    print(f'no of equal channels:{equals}')

#5 - conversion to the YCbCr model

RGB2YCBCR=np.array([[0.299,0.587,0.114],[-0.168736, -0.331264, 0.5],[0.5, -0.418688, -0.081312]])
YCBCR2RGB=np.linalg.inv(RGB2YCBCR)
min_cb = (0.5,0.5,0)
max_cb = (0.5,0.5,1)
min_cr = (0,0.5,0.5)
max_cr = (1,0.5,0.5)
cmGray = color_map('myGray', (0,0,0),(1,1,1) )
cmChromBlue = color_map('myCb', tuple(min_cb),  tuple(max_cb) )
cmChromRed = color_map('myCr', tuple(min_cr),  tuple(max_cr) )

def rbg2ycbcr(img):
    ycc= img.dot(RGB2YCBCR.T)
    ycc[:,:,1:3] += 128
    return ycc

def ycbcr2rgb(img):
    
    img[:,:,1:3] -= 128
    recovered = img.dot(YCBCR2RGB.T)
    recovered[recovered < 0]=0
    recovered[recovered > 255]=255
    recovered= np.round(recovered)
    return recovered.astype(np.uint8)

def ex5():
    chromin_image = rbg2ycbcr(img_bm_padded)
    
    y, cb,cr = separate_3channels(chromin_image)

    d = {'gray': cmGray,  'chromBlue':cmChromBlue, 'chromRed':cmChromRed }
    e = {'gray': y, 'chromBlue':cb, 'chromRed':cr}
    for col in d.keys():
        view_image(e[col],d[col],col)
    return y,cb,cr


def ycrcb_downsampling_cv2(y,cr,cb, comp_ratio): # comp_ratio is a tuple with 3 values, such as (4,2,2)
    cr_d=cb_d=np.array([])
    sh = y.shape
    lines, columns = sh[0], sh[1]
    print(sh)
    if comp_ratio[2]!= 0: #horizontal only
        cr_ratio= comp_ratio[0]//comp_ratio[1]
        columns /= cr_ratio
        cr_d = cv2.resize(cr, None, fx = 1/cr_ratio, fy=1, interpolation = cv2.INTER_AREA )
        cb_ratio = comp_ratio[0]//comp_ratio[2]
        columns = sh[1]//cb_ratio
        cb_d = cv2.resize(cb, None, fx = 1/cb_ratio, fy=1, interpolation = cv2.INTER_AREA )

    else: #  horizontal and vertical
        cb_ratio=cr_ratio=comp_ratio[0]//comp_ratio[1]
        lines /= cr_ratio
        columns /= cr_ratio
        cr_d = cv2.resize(cr, None, fx = 1/cr_ratio, fy=1/cr_ratio, interpolation = cv2.INTER_AREA )
        cb_d = cv2.resize(cb, None, fx = 1/cb_ratio, fy=1/cb_ratio, interpolation = cv2.INTER_AREA )
    return y, cr_d, cb_d

def ycrcb_upsampling_cv2(y,cr,cb, comp_ratio): # comp_ratio is a tuple with 3 values, such as (4,2,2)
    cr_u=cb_u=np.array([])
    sh = y.shape
    lines, columns = sh[0], sh[1]
    cr_lines, cr_columns = cr.shape
    cb_lines, cb_columns = cb.shape
    cr_u = cv2.resize(cr, None, fx = columns/cr_columns, fy=lines/cr_lines, interpolation = cv2.INTER_AREA )
    cb_u = cv2.resize(cb, None, fx = columns/cb_columns, fy=lines/cb_lines, interpolation = cv2.INTER_AREA )
    return y, cr_u, cb_u


def ex6():
    ds_ratio = (4,2,0)
    y_d,cr_d,cb_d = ycrcb_downsampling_cv2(y,cr,cb, ds_ratio)
    d = {'gray': cmGray,  'chromBlue':cmChromBlue, 'chromRed':cmChromRed }
    e = {'gray': y_d, 'chromBlue':cb_d, 'chromRed':cr_d}

    for col in d.keys():
        view_image(e[col],d[col],"%s with shape %s"%(col,e[col].shape))
    (y_u,cr_u,cb_u) = ycrcb_upsampling_cv2(y_d,cr_d,cb_d, ds_ratio)
        
    return y_d, cb_d, cr_d


#7 - DCT
def dct_array(channel):
    return dct(dct(channel, norm="ortho").T, norm='ortho').T
    #ans=np.round(ans)
    #return ans.astype(np.uint8)

def idct_array(dct_channel_arr):
    ans = idct(idct(dct_channel_arr, norm="ortho").T, norm='ortho').T
    # ans=np.round(ans)
    # ans[ans>255] =255
    # ans[ans<0] =0
    return ans

def dct_image(y_d, cb_d, cr_d):
    dct_y  = dct_array(y_d)
    dct_cb = dct_array(cb_d)
    dct_cr = dct_array(cr_d)
    return dct_y, dct_cb, dct_cr

def idct_image(dct_y, dct_cb, dct_cr):
    idct_y  = idct_array(dct_y)
    idct_cb = idct_array(dct_cb)
    idct_cr = idct_array(dct_cr)
    return idct_y, idct_cb, idct_cr

def ex7():
    dct_y, dct_cb, dct_cr = dct_image(y_d, cb_d, cr_d)
    dcts= {"y":dct_y,"cb":dct_cb,"cr":dct_cr}
    idct_y, idct_cb, idct_cr = idct_image(dct_y, dct_cb, dct_cr)
    idcts = {"y":idct_y,"cb":idct_cb,"cr":idct_cr}
    for name, channel in dcts.items():
        fig = plt.figure()
        # plt.title(f"{name} dct - log(2*x/sqrt(M*N)+0.0001)")
        channel_size =channel.shape[0] * channel.shape[1]
        # sh = plt.imshow(np.log(np.abs(2*channel/math.sqrt(channel_size)) + 0.0001))
        plt.title(f"{name} dct - log(abs(x)+0.0001)")
        sh = plt.imshow(np.log(np.abs(channel) + 0.0001))
        fig.colorbar(sh)
        plt.show(block=False)
    eqs1 = [idct_y,idct_cb,idct_cr]
    eqs2 = [y_d,cb_d,cr_d]
    equals=0
    for i in range(len(eqs1)):
        howmany=np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001)
        if howmany==0: 
            equals +=1
        else:
            print('no of different pixels: ',np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001))
            view_image(eqs1[i],cmGray)
            view_image(eqs2[i],cmGray)
            view_image(eqs2[i]-eqs1[i],cmGray)
    print('No. of equal channels: ',equals)



def dct_channel_by_blocks(channel, bs):
    sh =channel.shape
    ans= np.zeros(channel.shape)
    for i in range(0,sh[0],bs):
        for j in range(0,sh[1],bs):
            portion = channel[i:i+bs, j:j+bs]
            ans[i:i+bs, j:j+bs] = dct_array(portion)
    return ans

def dct_by_blocks(y_d, cb_d, cr_d,bs):
    y_dct = dct_channel_by_blocks(y_d, bs)
    cb_dct= dct_channel_by_blocks(cb_d, bs)
    cr_dct= dct_channel_by_blocks(cr_d, bs)
    return y_dct, cb_dct, cr_dct

def idct_channel_by_blocks(dct_image, bs):
    sh =dct_image.shape
    ans= np.zeros(dct_image.shape)
    for i in range(0,sh[0],bs):
        for j in range(0,sh[1],bs):
            portion = dct_image[i:i+bs, j:j+bs]
            ans[i:i+bs, j:j+bs] = idct_array(portion)
    return ans

def idct_by_blocks(y_dct, cb_dct, cr_dct,bs):
    y_idct = idct_channel_by_blocks(y_dct, bs)
    cb_idct= idct_channel_by_blocks(cb_dct, bs)
    cr_idct= idct_channel_by_blocks(cr_dct, bs)
    return y_idct, cb_idct, cr_idct

def ex7_23(y_d, cb_d, cr_d, bs):
    y_dct, cb_dct, cr_dct =  dct_by_blocks(y_d, cb_d, cr_d, bs)
    arrplot= [('y',y_dct),('cb',cb_dct),('cr',cr_dct)]
    for s,p in arrplot:
        view_image(np.log(np.abs(p)+0.0001),cmGray,s)
    y_idct, cb_idct,cr_idct =  idct_by_blocks( y_dct, cb_dct, cr_dct,bs)
    arrplot= [('y',cmGray,y_idct),('cb',cmChromBlue,cb_idct),('cr',cmChromRed,cr_idct)]
    for s,c,p in arrplot: # Visualization of the inverse images
        view_image(p,c,s)
    eqs1 = [y_idct,cb_idct,cr_idct]
    eqs2 = [y_d,cb_d,cr_d]
    equals=0
    for i in range(len(eqs1)):
        howmany=np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001)
        if howmany==0: 
            equals +=1
        else:
            print('no of different pixels: ',np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001))
            view_image(eqs1[i],cmGray)
            view_image(eqs2[i],cmGray)
            view_image(eqs2[i]-eqs1[i],cmGray)
    print('No. of equal channels: ',equals)
    return y_dct, cb_dct, cr_dct

QUANTIZATION_MATRIX_Y = np.array([[16,11,10,16,24,40,51,61],[12,12,14,19,26,58,60,55],[14,13,16,24,40,57,69,56],[14,17,22,29,51,87,80,62],[18,22,37,56,68,109,103,77],[24,35,55,64,81,104,113,92],[49,64,78,87,103,121,120,101],[72,92,95,98,112,100,103,99]]).astype(np.uint8)
QUANTIZATION_MATRIX_CBCR = np.array([[17,18,24,47,99,99,99,99],[18,21,26,66,99,99,99,99],[24,26,56,99,99,99,99,99],[47,66,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99]]).astype(np.uint8)

def repeat_matrix(m, rows, columns):
    return np.tile(m, (rows, columns)) # makes a larger matrix by repeating m throughout the two axes


def calculate_quant(matrix, channel):
    big_matrix = repeat_matrix(matrix, channel.shape[0]//matrix.shape[0], channel.shape[1]//matrix.shape[1])
    return np.round(channel/big_matrix)


def quality_factor_matrix(quality):
    Q_y = np.array(QUANTIZATION_MATRIX_Y)
    Q_cbcr = np.array(QUANTIZATION_MATRIX_CBCR)
    if( quality < 50):
        qual_factor = 50/quality
    else:
        qual_factor = (100-quality)/50

    if qual_factor == 0:
        Q_y=np.ones((8,8))
        Q_cbcr=np.ones((8,8))
    else:
        Q_y = np.round(Q_y * qual_factor)
        Q_y[Q_y>255]= 255
        Q_y[Q_y<0]=0
        Q_y=Q_y.astype(np.uint8)
        Q_cbcr = np.round(Q_cbcr * qual_factor)
        Q_cbcr[Q_cbcr>255]= 255
        Q_cbcr[Q_cbcr<0]=0
        Q_cbcr=Q_cbcr.astype(np.uint8)
    return Q_y, Q_cbcr
        

def quantize(y_dct, cb_dct, cr_dct , quality):

    Q_y, Q_cbcr= quality_factor_matrix(quality)

    y_q = calculate_quant(Q_y, y_dct)
    cb_q = calculate_quant(Q_cbcr, cb_dct)
    cr_q = calculate_quant(Q_cbcr, cr_dct)
    return y_q, cb_q, cr_q


def calculate_inverse_quant(matrix, channel):
    big_matrix = repeat_matrix(matrix, channel.shape[0] // matrix.shape[0], channel.shape[1]// matrix.shape[1])
    return np.round(channel * big_matrix)


def inverse_quant(y_q, cb_q, cr_q, quality):

    Q_y, Q_cbcr= quality_factor_matrix(quality)

    i_y_q = calculate_inverse_quant(Q_y, y_q)
    i_cb_q = calculate_inverse_quant(Q_cbcr, cb_q)
    i_cr_q = calculate_inverse_quant(Q_cbcr, cr_q)

    return i_y_q, i_cb_q, i_cr_q


# assuming we only use 8x8 blocks
def DPCM_channel(channel, bs):
    # sh=channel.shape
    aux_channel = np.array(channel)
    aux_channel[::bs,bs::bs] -= channel[::bs,0:-bs:bs]
    aux_channel[bs::bs,0] -= channel[0:-bs:bs,-bs]
    return aux_channel    

def DPCM(y_q, cb_q, cr_q, bs):
    dpcm_y = DPCM_channel(y_q, bs)
    dpcm_cb = DPCM_channel(cb_q, bs)
    dpcm_cr = DPCM_channel(cr_q, bs)
    return dpcm_y, dpcm_cb, dpcm_cr

def IDPCM_channel(channel, bs):
    copy = np.array(channel)
    sh= channel.shape
    aid = channel[::bs,::bs].flatten()
    aid = np.cumsum(aid).reshape(sh[0]//bs,sh[1]//bs)
    copy[::bs,::bs] = aid
    return copy

def IDPCM(dpcm_y, dpcm_cb, dpcm_cr,bs):
    idpcm_y = IDPCM_channel(dpcm_y, bs)
    idpcm_cb = IDPCM_channel(dpcm_cb, bs)
    idpcm_cr = IDPCM_channel(dpcm_cr, bs)
    return idpcm_y, idpcm_cb, idpcm_cr

def encode(img_name, ds_rate: tuple, quality, bs=8) -> None:
    img= plt.imread(img_name)
    img_padded , original_shape = image_padding(img, ds_rate, bs)
    chromin_image = rbg2ycbcr(img_padded)
    y, cb,cr = separate_3channels(chromin_image)
    plot_debug(y, cb,cr)
    y_d,cr_d,cb_d = ycrcb_downsampling_cv2(y,cr,cb, ds_rate)
    plot_debug(y_d,cr_d,cb_d)
    dct_y, dct_cb, dct_cr = dct_by_blocks(y_d, cb_d, cr_d ,bs)
    plot_debug(dct_y, dct_cb, dct_cr)
    y_q, cb_q, cr_q = quantize(dct_y,dct_cb,dct_cr, quality)
    plot_debug(y_q, cb_q, cr_q)
    dpcm_y, dpcm_cb, dpcm_cr = DPCM(y_q, cb_q, cr_q, bs)
    plot_debug(dpcm_y, dpcm_cb, dpcm_cr)
    return dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate

def decode(dpcm_y, dpcm_cb, dpcm_cr, original_shape, quality, bs, ds_rate):
    idpcm_y, idpcm_cb, idpcm_cr = IDPCM(dpcm_y, dpcm_cb, dpcm_cr,bs)
    plot_debug(idpcm_y, idpcm_cb, idpcm_cr)
    i_y_q, i_cb_q, i_cr_q = inverse_quant(idpcm_y, idpcm_cb, idpcm_cr , quality=quality)

    plot_debug(i_y_q, i_cb_q, i_cr_q)
    y_d, cb_d, cr_d = idct_by_blocks(i_y_q, i_cb_q, i_cr_q, bs ) 

    plot_debug(y_d, cb_d, cr_d)
    y_u,cr_u, cb_u = ycrcb_upsampling_cv2(y_d,cr_d, cb_d, ds_rate)
    
    plot_debug(y_u,cr_u, cb_u)
    encoded= join_3channels(y_u,cb_u,cr_u)

    inverse_chromin = ycbcr2rgb(encoded)

    img = image_remove_padding(inverse_chromin, original_shape)


    return img

def main():
    ds_ratio = (4,2,2)
    bs=8
    quality=75
    dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate = encode('imagens/peppers.bmp', ds_ratio, quality, 8)
    decoded= decode(dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate)
    #decoded = encode_and_decode('imagens/peppers.bmp', ds_ratio, quality, 8)
    plt.figure()
    plt.title('Post decoce')
    plt.imshow(decoded,cmGray)
    plt.show(block=False)

if __name__ == "__main__":
    main()
    input()def idct_array(dct_channel_arr):
    ans = idct(idct(dct_channel_arr, norm="ortho").T, norm='ortho').T
    # ans=np.round(ans)
    # ans[ans>255] =255
    # ans[ans<0] =0
    return ans

def dct_image(y_d, cb_d, cr_d):
    dct_y  = dct_array(y_d)
    dct_cb = dct_array(cb_d)
    dct_cr = dct_array(cr_d)
    return dct_y, dct_cb, dct_cr

def idct_image(dct_y, dct_cb, dct_cr):
    idct_y  = idct_array(dct_y)
    idct_cb = idct_array(dct_cb)
    idct_cr = idct_array(dct_cr)
    return idct_y, idct_cb, idct_cr

def ex7():
    dct_y, dct_cb, dct_cr = dct_image(y_d, cb_d, cr_d)
    dcts= {"y":dct_y,"cb":dct_cb,"cr":dct_cr}
    idct_y, idct_cb, idct_cr = idct_image(dct_y, dct_cb, dct_cr)
    idcts = {"y":idct_y,"cb":idct_cb,"cr":idct_cr}
    for name, channel in dcts.items():
        fig = plt.figure()
        # plt.title(f"{name} dct - log(2*x/sqrt(M*N)+0.0001)")
        channel_size =channel.shape[0] * channel.shape[1]
        # sh = plt.imshow(np.log(np.abs(2*channel/math.sqrt(channel_size)) + 0.0001))
        plt.title(f"{name} dct - log(abs(x)+0.0001)")
        sh = plt.imshow(np.log(np.abs(channel) + 0.0001))
        fig.colorbar(sh)
        plt.show(block=False)
    eqs1 = [idct_y,idct_cb,idct_cr]
    eqs2 = [y_d,cb_d,cr_d]
    equals=0
    for i in range(len(eqs1)):
        howmany=np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001)
        if howmany==0: 
            equals +=1
        else:
            print('no of different pixels: ',np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001))
            view_image(eqs1[i],cmGray)
            view_image(eqs2[i],cmGray)
            view_image(eqs2[i]-eqs1[i],cmGray)
    print('No. of equal channels: ',equals)



def dct_channel_by_blocks(channel, bs):
    sh =channel.shape
    ans= np.zeros(channel.shape)
    for i in range(0,sh[0],bs):
        for j in range(0,sh[1],bs):
            portion = channel[i:i+bs, j:j+bs]
            ans[i:i+bs, j:j+bs] = dct_array(portion)
    return ans

def dct_by_blocks(y_d, cb_d, cr_d,bs):
    y_dct = dct_channel_by_blocks(y_d, bs)
    cb_dct= dct_channel_by_blocks(cb_d, bs)
    cr_dct= dct_channel_by_blocks(cr_d, bs)
    return y_dct, cb_dct, cr_dct

def idct_channel_by_blocks(dct_image, bs):
    sh =dct_image.shape
    ans= np.zeros(dct_image.shape)
    for i in range(0,sh[0],bs):
        for j in range(0,sh[1],bs):
            portion = dct_image[i:i+bs, j:j+bs]
            ans[i:i+bs, j:j+bs] = idct_array(portion)
    return ans

def idct_by_blocks(y_dct, cb_dct, cr_dct,bs):
    y_idct = idct_channel_by_blocks(y_dct, bs)
    cb_idct= idct_channel_by_blocks(cb_dct, bs)
    cr_idct= idct_channel_by_blocks(cr_dct, bs)
    return y_idct, cb_idct, cr_idct

def ex7_23(y_d, cb_d, cr_d, bs):
    y_dct, cb_dct, cr_dct =  dct_by_blocks(y_d, cb_d, cr_d, bs)
    arrplot= [('y',y_dct),('cb',cb_dct),('cr',cr_dct)]
    for s,p in arrplot:
        view_image(np.log(np.abs(p)+0.0001),cmGray,s)
    y_idct, cb_idct,cr_idct =  idct_by_blocks( y_dct, cb_dct, cr_dct,bs)
    arrplot= [('y',cmGray,y_idct),('cb',cmChromBlue,cb_idct),('cr',cmChromRed,cr_idct)]
    for s,c,p in arrplot: # Visualization of the inverse images
        view_image(p,c,s)
    eqs1 = [y_idct,cb_idct,cr_idct]
    eqs2 = [y_d,cb_d,cr_d]
    equals=0
    for i in range(len(eqs1)):
        howmany=np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001)
        if howmany==0: 
            equals +=1
        else:
            print('no of different pixels: ',np.count_nonzero(np.abs(eqs1[i]-eqs2[i])>0.000001))
            view_image(eqs1[i],cmGray)
            view_image(eqs2[i],cmGray)
            view_image(eqs2[i]-eqs1[i],cmGray)
    print('No. of equal channels: ',equals)
    return y_dct, cb_dct, cr_dct

QUANTIZATION_MATRIX_Y = np.array([[16,11,10,16,24,40,51,61],[12,12,14,19,26,58,60,55],[14,13,16,24,40,57,69,56],[14,17,22,29,51,87,80,62],[18,22,37,56,68,109,103,77],[24,35,55,64,81,104,113,92],[49,64,78,87,103,121,120,101],[72,92,95,98,112,100,103,99]]).astype(np.uint8)
QUANTIZATION_MATRIX_CBCR = np.array([[17,18,24,47,99,99,99,99],[18,21,26,66,99,99,99,99],[24,26,56,99,99,99,99,99],[47,66,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99],[99,99,99,99,99,99,99,99]]).astype(np.uint8)

def repeat_matrix(m, rows, columns):
    return np.tile(m, (rows, columns)) # makes a larger matrix by repeating m throughout the two axes


def calculate_quant(matrix, channel):
    big_matrix = repeat_matrix(matrix, channel.shape[0]//matrix.shape[0], channel.shape[1]//matrix.shape[1])
    return np.round(channel/big_matrix)


def quality_factor_matrix(quality):
    Q_y = np.array(QUANTIZATION_MATRIX_Y)
    Q_cbcr = np.array(QUANTIZATION_MATRIX_CBCR)
    if( quality < 50):
        qual_factor = 50/quality
    else:
        qual_factor = (100-quality)/50

    if qual_factor == 0:
        Q_y=np.ones((8,8))
        Q_cbcr=np.ones((8,8))
    else:
        Q_y = np.round(Q_y * qual_factor)
        Q_y[Q_y>255]= 255
        Q_y[Q_y<0]=0
        Q_y=Q_y.astype(np.uint8)
        Q_cbcr = np.round(Q_cbcr * qual_factor)
        Q_cbcr[Q_cbcr>255]= 255
        Q_cbcr[Q_cbcr<0]=0
        Q_cbcr=Q_cbcr.astype(np.uint8)
    return Q_y, Q_cbcr
        

def quantize(y_dct, cb_dct, cr_dct , quality):

    Q_y, Q_cbcr= quality_factor_matrix(quality)

    y_q = calculate_quant(Q_y, y_dct)
    cb_q = calculate_quant(Q_cbcr, cb_dct)
    cr_q = calculate_quant(Q_cbcr, cr_dct)
    return y_q, cb_q, cr_q


def calculate_inverse_quant(matrix, channel):
    big_matrix = repeat_matrix(matrix, channel.shape[0] // matrix.shape[0], channel.shape[1]// matrix.shape[1])
    return np.round(channel * big_matrix)


def inverse_quant(y_q, cb_q, cr_q, quality):

    Q_y, Q_cbcr= quality_factor_matrix(quality)

    i_y_q = calculate_inverse_quant(Q_y, y_q)
    i_cb_q = calculate_inverse_quant(Q_cbcr, cb_q)
    i_cr_q = calculate_inverse_quant(Q_cbcr, cr_q)

    return i_y_q, i_cb_q, i_cr_q


# assuming we only use 8x8 blocks
def DPCM_channel(channel, bs):
    # sh=channel.shape
    aux_channel = np.array(channel)
    aux_channel[::bs,bs::bs] -= channel[::bs,0:-bs:bs]
    aux_channel[bs::bs,0] -= channel[0:-bs:bs,-bs]
    return aux_channel    

def DPCM(y_q, cb_q, cr_q, bs):
    dpcm_y = DPCM_channel(y_q, bs)
    dpcm_cb = DPCM_channel(cb_q, bs)
    dpcm_cr = DPCM_channel(cr_q, bs)
    return dpcm_y, dpcm_cb, dpcm_cr

def IDPCM_channel(channel, bs):
    copy = np.array(channel)
    sh= channel.shape
    aid = channel[::bs,::bs].flatten()
    aid = np.cumsum(aid).reshape(sh[0]//bs,sh[1]//bs)
    copy[::bs,::bs] = aid
    return copy

def IDPCM(dpcm_y, dpcm_cb, dpcm_cr,bs):
    idpcm_y = IDPCM_channel(dpcm_y, bs)
    idpcm_cb = IDPCM_channel(dpcm_cb, bs)
    idpcm_cr = IDPCM_channel(dpcm_cr, bs)
    return idpcm_y, idpcm_cb, idpcm_cr

def encode(img_name, ds_rate: tuple, quality, bs=8) -> None:
    img= plt.imread(img_name)
    img_padded , original_shape = image_padding(img, ds_rate, bs)
    chromin_image = rbg2ycbcr(img_padded)
    y, cb,cr = separate_3channels(chromin_image)
    plot_debug(y, cb,cr)
    y_d,cr_d,cb_d = ycrcb_downsampling_cv2(y,cr,cb, ds_rate)
    plot_debug(y_d,cr_d,cb_d)
    dct_y, dct_cb, dct_cr = dct_by_blocks(y_d, cb_d, cr_d ,bs)
    plot_debug(dct_y, dct_cb, dct_cr)
    y_q, cb_q, cr_q = quantize(dct_y,dct_cb,dct_cr, quality)
    plot_debug(y_q, cb_q, cr_q)
    dpcm_y, dpcm_cb, dpcm_cr = DPCM(y_q, cb_q, cr_q, bs)
    plot_debug(dpcm_y, dpcm_cb, dpcm_cr)
    return dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate

def decode(dpcm_y, dpcm_cb, dpcm_cr, original_shape, quality, bs, ds_rate):
    idpcm_y, idpcm_cb, idpcm_cr = IDPCM(dpcm_y, dpcm_cb, dpcm_cr,bs)
    plot_debug(idpcm_y, idpcm_cb, idpcm_cr)
    i_y_q, i_cb_q, i_cr_q = inverse_quant(idpcm_y, idpcm_cb, idpcm_cr , quality=quality)

    plot_debug(i_y_q, i_cb_q, i_cr_q)
    y_d, cb_d, cr_d = idct_by_blocks(i_y_q, i_cb_q, i_cr_q, bs ) 

    plot_debug(y_d, cb_d, cr_d)
    y_u,cr_u, cb_u = ycrcb_upsampling_cv2(y_d,cr_d, cb_d, ds_rate)
    
    plot_debug(y_u,cr_u, cb_u)
    encoded= join_3channels(y_u,cb_u,cr_u)

    inverse_chromin = ycbcr2rgb(encoded)

    img = image_remove_padding(inverse_chromin, original_shape)


    return img

def main():
    ds_ratio = (4,2,2)
    bs=8
    quality=75
    dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate = encode('imagens/peppers.bmp', ds_ratio, quality, 8)
    decoded= decode(dpcm_y, dpcm_cb, dpcm_cr , original_shape, quality, bs, ds_rate)
    #decoded = encode_and_decode('imagens/peppers.bmp', ds_ratio, quality, 8)
    plt.figure()
    plt.title('Post decoce')
    plt.imshow(decoded,cmGray)
    plt.show(block=False)

if __name__ == "__main__":
    main()
    input()
