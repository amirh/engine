// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "content_aware_canvas.h"
#include "third_party/skia/src/core/SkCanvasPriv.h"

namespace flutter {

ContentAwareCanvas::ContentAwareCanvas(SkCanvas* targetCanvas, int width, int height) : INHERITED(width, height) {
  isEmpty_  = true;
  targetCanvas_ = targetCanvas;
}

///////////////////////////////////////////////////////////////////////////
// These are forwarded to the target canvas we're referencing


void ContentAwareCanvas::willSave() {
  targetCanvas_->save();
  this->INHERITED::willSave();
}

SkCanvas::SaveLayerStrategy ContentAwareCanvas::getSaveLayerStrategy(const SaveLayerRec& rec) {
    targetCanvas_->saveLayer(rec);

    this->INHERITED::getSaveLayerStrategy(rec);

    return kNoLayer_SaveLayerStrategy;
}

bool ContentAwareCanvas::onDoSaveBehind(const SkRect* bounds) {
  SkCanvasPriv::SaveBehind(targetCanvas_, bounds);
  this->INHERITED::onDoSaveBehind(bounds);
  return false;
}

void ContentAwareCanvas::willRestore() {
    targetCanvas_->restore();
    this->INHERITED::willRestore();
}

void ContentAwareCanvas::didConcat(const SkMatrix& matrix) {
    targetCanvas_->concat(matrix);
    this->INHERITED::didConcat(matrix);
}

void ContentAwareCanvas::didSetMatrix(const SkMatrix& matrix) {
    targetCanvas_->setMatrix(matrix);
    this->INHERITED::didSetMatrix(matrix);
}

void ContentAwareCanvas::onClipRect(const SkRect& rect, SkClipOp op, ClipEdgeStyle edgeStyle) {
    targetCanvas_->clipRect(rect, op, kSoft_ClipEdgeStyle == edgeStyle);
    this->INHERITED::onClipRect(rect, op, edgeStyle);
}

void ContentAwareCanvas::onClipRRect(const SkRRect& rrect, SkClipOp op, ClipEdgeStyle edgeStyle) {
    targetCanvas_->clipRRect(rrect, op, kSoft_ClipEdgeStyle == edgeStyle);
    this->INHERITED::onClipRRect(rrect, op, edgeStyle);
}

void ContentAwareCanvas::onClipPath(const SkPath& path, SkClipOp op, ClipEdgeStyle edgeStyle) {
    targetCanvas_->clipPath(path, op, kSoft_ClipEdgeStyle == edgeStyle);
    this->INHERITED::onClipPath(path, op, edgeStyle);
}

void ContentAwareCanvas::onClipRegion(const SkRegion& deviceRgn, SkClipOp op) {
    targetCanvas_->clipRegion(deviceRgn, op);
    this->INHERITED::onClipRegion(deviceRgn, op);
}

void ContentAwareCanvas::onDrawPaint(const SkPaint& paint) {
  targetCanvas_->drawPaint(paint);
}

void ContentAwareCanvas::onDrawBehind(const SkPaint& paint) {
   SkCanvasPriv::DrawBehind(targetCanvas_, paint);
}

void ContentAwareCanvas::onDrawPoints(PointMode mode, size_t count, const SkPoint pts[],
                                const SkPaint& paint) {
  targetCanvas_->drawPoints(mode, count, pts, paint);
}

void ContentAwareCanvas::onDrawRect(const SkRect& rect, const SkPaint& paint) {
  targetCanvas_->drawRect(rect, paint);
}

void ContentAwareCanvas::onDrawRegion(const SkRegion& region, const SkPaint& paint) {
  targetCanvas_->drawRegion(region, paint);
}

void ContentAwareCanvas::onDrawOval(const SkRect& rect, const SkPaint& paint) {
  targetCanvas_->drawOval(rect, paint);
}

void ContentAwareCanvas::onDrawArc(const SkRect& rect, SkScalar startAngle, SkScalar sweepAngle,
                             bool useCenter, const SkPaint& paint) {
  targetCanvas_->drawArc(rect, startAngle, sweepAngle, useCenter,  paint);
}

void ContentAwareCanvas::onDrawRRect(const SkRRect& rrect, const SkPaint& paint) {
  targetCanvas_->drawRRect(rrect, paint);
}

void ContentAwareCanvas::onDrawDRRect(const SkRRect& outer, const SkRRect& inner, const SkPaint& paint) {
  targetCanvas_->drawDRRect(outer, inner, paint);
}

void ContentAwareCanvas::onDrawPath(const SkPath& path, const SkPaint& paint) {
  targetCanvas_->drawPath(path, paint)
}

void ContentAwareCanvas::onDrawBitmap(const SkBitmap& bitmap, SkScalar x, SkScalar y,
                                const SkPaint* paint) {
  targetCanvas_->drawBitmap(bitmap, x, y, paint);
}

void ContentAwareCanvas::onDrawBitmapRect(const SkBitmap& bitmap, const SkRect* src, const SkRect& dst,
                                    const SkPaint* paint, SrcRectConstraint constraint) {
  targetCanvas_->drawBitmapRect(bitmap, src, dst, paint, constraint);
}

void ContentAwareCanvas::onDrawBitmapNine(const SkBitmap& bitmap, const SkIRect& center,
                                    const SkRect& dst, const SkPaint* paint) {
  targetCanvas_->onDrawBitmapNine(bitmap, center, dst, paint);
}

void ContentAwareCanvas::onDrawBitmapLattice(const SkBitmap& bitmap, const Lattice& lattice,
                                       const SkRect& dst, const SkPaint* paint) {
  targetCanvas_->onDrawBitmapLattice(bitmap, lattice, dst, paint);
}

void ContentAwareCanvas::onDrawImage(const SkImage* image, SkScalar left, SkScalar top,
                               const SkPaint* paint) {
  targetCanvas_->onDrawImage(image, left, top, paint);
}

void ContentAwareCanvas::onDrawImageRect(const SkImage* image, const SkRect* src, const SkRect& dst,
                                   const SkPaint* paint, SrcRectConstraint constraint) {
  targetCanvas_->onDrawImageRect(image, src, dst, paint, constraint);
}

void ContentAwareCanvas::onDrawImageNine(const SkImage* image, const SkIRect& center, const SkRect& dst,
                                   const SkPaint* paint) {
  targetCanvas_->onDrawImageNine(image, center, dst, paint);
}

void ContentAwareCanvas::onDrawImageLattice(const SkImage* image, const Lattice& lattice,
                                      const SkRect& dst, const SkPaint* paint) {
  targetCanvas_->onDrawImageLattice(image, lattice, dst, paint);
}

void ContentAwareCanvas::onDrawTextBlob(const SkTextBlob* blob, SkScalar x, SkScalar y,
                                  const SkPaint &paint) {
  targetCanvas_->onDrawTextBlob(blob, x, y, &paint);
}

void ContentAwareCanvas::onDrawPicture(const SkPicture* picture, const SkMatrix* matrix,
                                 const SkPaint* paint) {
  targetCanvas_->onDrawPicture(picture, matrix, paint);
}

void ContentAwareCanvas::onDrawDrawable(SkDrawable* drawable, const SkMatrix* matrix) {
  targetCanvas_->onDrawDrawable(drawable, matrix);
}

void ContentAwareCanvas::onDrawVerticesObject(const SkVertices* vertices, const SkVertices::Bone bones[],
                                        int boneCount, SkBlendMode bmode, const SkPaint& paint) {
  targetCanvas_->onDrawVerticesObject(vertices, bones, boneCount, bmode, SkPaint& paint)
}

void ContentAwareCanvas::onDrawPatch(const SkPoint cubics[12], const SkColor colors[4],
                               const SkPoint texCoords[4], SkBlendMode bmode,
                               const SkPaint& paint) {
  targetCanvas_->onDrawPatch(cubics, colors, texCoords, bmode, paint);
}

void ContentAwareCanvas::onDrawAtlas(const SkImage* image, const SkRSXform xform[], const SkRect tex[],
                               const SkColor colors[], int count, SkBlendMode bmode,
                               const SkRect* cull, const SkPaint* paint) {
  targetCanvas_->onDrawAtlas(image, xform, tex, colors, count, bmode, cull, paint);
}

void ContentAwareCanvas::onDrawShadowRec(const SkPath& path, const SkDrawShadowRec& rec) {
  targetCanvas_->onDrawShadowRec(path, rec);
}

void ContentAwareCanvas::onDrawAnnotation(const SkRect& rect, const char key[], SkData* data) {
  targetCanvas_->onDrawAnnotation(rect, key, data);
}

void ContentAwareCanvas::onDrawEdgeAAQuad(const SkRect& rect, const SkPoint clip[4],
                                    QuadAAFlags aa, SkColor color, SkBlendMode mode) {
  targetCanvas_->onDrawEdgeAAQuad(rect, clip, aa, color, mode);
}

void ContentAwareCanvas::onDrawEdgeAAImageSet(const ImageSetEntry set[], int count,
                                        const SkPoint dstClips[], const SkMatrix preViewMatrices[],
                                        const SkPaint* paint, SrcRectConstraint constraint) {
  targetCanvas_->onDrawEdgeAAImageSet(set, count, dstClips, preViewMatrices, paint, constraint);
}

void ContentAwareCanvas::onFlush() {
  targetCanvas_->onFlush();
}
}  // namespace flutter
