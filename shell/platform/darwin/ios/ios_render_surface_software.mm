// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "flutter/shell/platform/darwin/ios/ios_render_surface_software.h"

#include <QuartzCore/CALayer.h>

#include <memory>

#include "flutter/fml/logging.h"
#include "flutter/fml/platform/darwin/cf_utils.h"
#include "flutter/fml/trace_event.h"
#include "third_party/skia/include/utils/mac/SkCGUtils.h"

namespace shell {

IOSRenderSurfaceSoftware::IOSRenderSurfaceSoftware(fml::scoped_nsobject<CALayer> layer)
    : layer_(std::move(layer)) {
  UpdateStorageSizeIfNecessary();
}

IOSRenderSurfaceSoftware::~IOSRenderSurfaceSoftware() = default;

bool IOSRenderSurfaceSoftware::IsValid() const {
  return layer_;
}

bool IOSRenderSurfaceSoftware::ResourceContextMakeCurrent() {
  return false;
}

void IOSRenderSurfaceSoftware::UpdateStorageSizeIfNecessary() {
  // Nothing to do here. We don't need an external entity to tell us when our
  // backing store needs to be updated. Instead, we let the frame tell us its
  // size so we can update to match. This method was added to work around
  // Android oddities.
}

std::unique_ptr<Surface> IOSRenderSurfaceSoftware::CreateGPUSurface() {
  return nullptr;
}

sk_sp<SkSurface> IOSRenderSurfaceSoftware::AcquireBackingStore(const SkISize& size) {
  TRACE_EVENT0("flutter", "IOSRenderSurfaceSoftware::AcquireBackingStore");
  if (!IsValid()) {
    return nullptr;
  }

  if (sk_surface_ != nullptr &&
      SkISize::Make(sk_surface_->width(), sk_surface_->height()) == size) {
    // The old and new surface sizes are the same. Nothing to do here.
    return sk_surface_;
  }

  SkImageInfo info = SkImageInfo::MakeN32(size.fWidth, size.fHeight, kPremul_SkAlphaType);
  sk_surface_ = SkSurface::MakeRaster(info, nullptr);
  return sk_surface_;
}

bool IOSRenderSurfaceSoftware::PresentBackingStore(sk_sp<SkSurface> backing_store) {
  TRACE_EVENT0("flutter", "IOSRenderSurfaceSoftware::PresentBackingStore");
  if (!IsValid() || backing_store == nullptr) {
    return false;
  }

  SkPixmap pixmap;
  if (!backing_store->peekPixels(&pixmap)) {
    return false;
  }

  // Some basic sanity checking.
  uint64_t expected_pixmap_data_size = pixmap.width() * pixmap.height() * 4;

  const size_t pixmap_size = pixmap.computeByteSize();

  if (expected_pixmap_data_size != pixmap_size) {
    return false;
  }

  fml::CFRef<CGColorSpaceRef> colorspace(CGColorSpaceCreateDeviceRGB());

  // Setup the data provider that gives CG a view into the pixmap.
  fml::CFRef<CGDataProviderRef> pixmap_data_provider(CGDataProviderCreateWithData(
      nullptr,          // info
      pixmap.addr32(),  // data
      pixmap_size,      // size
      nullptr           // release callback
      ));

  if (!pixmap_data_provider) {
    return false;
  }

  // Create the CGImageRef representation on the pixmap.
  fml::CFRef<CGImageRef> pixmap_image(CGImageCreate(pixmap.width(),     // width
                                                    pixmap.height(),    // height
                                                    8,                  // bits per component
                                                    32,                 // bits per pixel
                                                    pixmap.rowBytes(),  // bytes per row
                                                    colorspace,         // colorspace
                                                    kCGImageAlphaPremultipliedLast,  // bitmap info
                                                    pixmap_data_provider,      // data provider
                                                    nullptr,                   // decode array
                                                    false,                     // should interpolate
                                                    kCGRenderingIntentDefault  // rendering intent
                                                    ));

  if (!pixmap_image) {
    return false;
  }

  layer_.get().contents = reinterpret_cast<id>(static_cast<CGImageRef>(pixmap_image));

  return true;
}

}  // namespace shell
