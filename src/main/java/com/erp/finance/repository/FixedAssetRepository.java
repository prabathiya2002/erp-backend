package com.erp.finance.repository;

import com.erp.finance.domain.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long> {
    List<FixedAsset> findByStatus(FixedAsset.AssetStatus status);
    List<FixedAsset> findByCategory(FixedAsset.AssetCategory category);
}
